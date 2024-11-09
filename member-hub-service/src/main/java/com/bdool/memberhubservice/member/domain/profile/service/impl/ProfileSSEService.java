package com.bdool.memberhubservice.member.domain.profile.service.impl;

import com.bdool.memberhubservice.member.domain.profile.entity.model.sse.ProfileNicknameResponse;
import com.bdool.memberhubservice.member.domain.profile.entity.model.sse.ProfileOnlineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Service
public class ProfileSSEService {

    // SseEmitter를 관리하는 ConcurrentHashMap (Thread-safe)
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, Queue<SseEvent>> eventQueue = new ConcurrentHashMap<>();  // 이벤트 큐를 관리하는 Map

    // SseEvent 클래스 정의
    private static class SseEvent {
        String eventName;
        Object data;

        public SseEvent(String eventName, Object data) {
            this.eventName = eventName;
            this.data = data;
        }
    }

    // 클라이언트가 SSE 구독 요청을 하면 호출
    public SseEmitter createSseEmitter() {
        Long emitterId = System.currentTimeMillis();
        SseEmitter emitter = new SseEmitter(30000L);  // 타임아웃을 30초로 설정
        emitters.put(emitterId, emitter);  // emitters에 새 구독자 추가
        eventQueue.putIfAbsent(emitterId, new ConcurrentLinkedQueue<>());  // 이벤트 큐 추가

        // 재연결 시 큐에 저장된 이벤트 전송
        emitter.onCompletion(() -> {
            emitters.remove(emitterId);  // 완료되면 emitters에서 제거
            eventQueue.remove(emitterId);  // 큐 제거
            System.out.println("SSE connection completed: " + emitterId);
        });

        // SSE 연결이 타임아웃될 때
        emitter.onTimeout(() -> {
            emitters.remove(emitterId);  // 타임아웃 발생 시 emitters에서 제거
            System.out.println("SSE connection timed out: " + emitterId);
            emitter.complete();  // 타임아웃이 발생하면 완료 처리
        });

        // SSE 연결 중 에러가 발생할 때
        emitter.onError(e -> {
            emitters.remove(emitterId);  // 에러 발생 시 emitters에서 제거
            System.err.println("SSE connection error: " + emitterId + ", error: " + e.getMessage());
            emitter.completeWithError(e);  // 에러가 발생하면 완료 처리
        });

        // 재연결 시 큐에 저장된 이벤트를 전송
        sendQueuedEvents(emitterId, emitter);

        return emitter;  // SseEmitter 반환
    }

    // 닉네임 변경 이벤트를 모든 구독자에게 전송
    public void notifyNicknameChange(ProfileNicknameResponse profileNicknameResponse) {
        sendEventToAllEmitters("nickname-change", profileNicknameResponse);
    }

    // 온라인 상태 변경 이벤트를 모든 구독자에게 전송
    public void notifyOnlineChange(ProfileOnlineResponse profileOnlineResponse) {
        sendEventToAllEmitters("online-status-change", profileOnlineResponse);
    }

    private void sendEventToAllEmitters(String eventName, Object data) {
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                System.err.println("Error sending event to emitter: " + id + ", error: " + e.getMessage());
                // 전송 실패 시 큐에 이벤트 저장
                Queue<SseEvent> queue = eventQueue.get(id);
                if (queue != null) {
                    queue.add(new SseEvent(eventName, data));
                }
                emitter.completeWithError(e);  // 에러 발생 시 해당 Emitter 종료
                emitters.remove(id);  // Emitter를 emitters 맵에서 제거
            }
        });
    }

    // 재연결 시 큐에 저장된 이벤트를 전송
    private void sendQueuedEvents(Long emitterId, SseEmitter emitter) {
        Queue<SseEvent> queue = eventQueue.get(emitterId);
        if (queue != null) {
            while (!queue.isEmpty()) {
                SseEvent event = queue.poll();  // 큐에서 이벤트를 가져옴
                try {
                    emitter.send(SseEmitter.event()
                            .name(event.eventName)
                            .data(event.data));  // 큐에 있던 이벤트 전송
                } catch (IOException e) {
                    System.err.println("Error sending queued event to emitter: " + emitterId + ", error: " + e.getMessage());
                    emitter.completeWithError(e);
                    break;
                }
            }
        }
    }
}