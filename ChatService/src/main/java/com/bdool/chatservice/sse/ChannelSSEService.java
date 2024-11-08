package com.bdool.chatservice.sse;

import com.bdool.chatservice.sse.model.ChannelAddResponse;
import com.bdool.chatservice.sse.model.ChannelDeleteResponse;
import com.bdool.chatservice.sse.model.ChannelRenameResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChannelSSEService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createSseEmitter() {
        SseEmitter emitter = new SseEmitter(0L);  // 타임아웃을 무한대로 설정
        Long emitterId = System.currentTimeMillis();
        emitters.put(emitterId, emitter);

        // SSE 연결이 정상적으로 종료될 때
        emitter.onCompletion(() -> {
            emitters.remove(emitterId);
            System.out.println("SSE connection completed: " + emitterId);
        });

        // SSE 연결이 타임아웃될 때
        emitter.onTimeout(() -> {
            emitters.remove(emitterId);
            System.out.println("SSE connection timed out: " + emitterId);
            emitter.complete();  // 타임아웃이 발생하면 완료 처리
        });

        // SSE 연결 중 에러가 발생할 때
        emitter.onError(e -> {
            emitters.remove(emitterId);
            System.err.println("SSE connection error: " + emitterId + ", error: " + e.getMessage());
            emitter.completeWithError(e);  // 에러가 발생하면 완료 처리
        });

        return emitter;  // SseEmitter 반환
    }

    // 채널 생성 이벤트 전송
    public void notifyChannelAdd(ChannelAddResponse channelAddResponse) {
        sendEventToAllEmitters("channel-add", channelAddResponse);
    }

    // 채널 이름 변경 이벤트 전송
    public void notifyChannelRename(ChannelRenameResponse channelRenameResponse) {
        sendEventToAllEmitters("channel-rename", channelRenameResponse);
    }
    // 채널 이름 변경 이벤트 전송
    public void notifyChannelDelete(ChannelDeleteResponse channelDeleteResponse) {
        sendEventToAllEmitters("channel-delete", channelDeleteResponse);
    }

    private void sendEventToAllEmitters(String eventName, Object data) {
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(id);
            }
        });
    }
}
