package com.bdool.documentservice.controller;

import com.bdool.documentservice.entity.DocumentUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class DocumentMessageController {

    private final Logger log = LoggerFactory.getLogger(DocumentMessageController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    private final Map<Long, StringBuilder> chunkStorage = new ConcurrentHashMap<>(); // 청크 저장소

    @MessageMapping("/editDocument")
    public void sendDocumentUpdate(DocumentUpdateMessage message) {
        Long documentId = message.getDocumentId();
        String chunk = message.getChunk();

        // Null 체크 추가
        if (documentId == null || chunk == null) {
            System.err.println("Received null documentId or chunk.");
            log.error("");
            return;
        }

        // 현재 청크 데이터를 저장소에 누적 저장 (없을 경우 새로운 StringBuilder 생성)
        StringBuilder currentContent = chunkStorage.computeIfAbsent(documentId, k -> new StringBuilder());
        currentContent.append(chunk);

        // 마지막 청크인지 확인
        if (message.getIsLastChunk()) {
            // 마지막 청크라면 전체 문서로 간주
            String fullContent = currentContent.toString();
            redisTemplate.opsForValue().set("document:" + documentId, fullContent);  // Redis에 저장
            log.info("\n"+redisTemplate.opsForValue().get("document:" + documentId));
            chunkStorage.remove(documentId); // 청크 저장소에서 제거
            System.out.println("Received full document: " + fullContent);

            // 클라이언트로 청크로 다시 분할해서 전송
            int chunkSize = 4092; // 1KB 청크 단위로 분할
            int totalChunks = (int) Math.ceil((double) fullContent.length() / chunkSize);

            for (int i = 0; i < totalChunks; i++) {
                int start = i * chunkSize;
                int end = Math.min((i + 1) * chunkSize, fullContent.length());

                // 안전한 substring 처리
                if (start < fullContent.length()) {
                    String contentChunk = fullContent.substring(start, end);
                    boolean isLastChunk = (i == totalChunks - 1);

                    // 청크 단위로 클라이언트에게 전송
                    messagingTemplate.convertAndSend("/topic/document/" + documentId,
                            new DocumentUpdateMessage(documentId, contentChunk, isLastChunk));
                    System.out.println("Sending chunk: " + (i + 1) + "/" + totalChunks + ", chunk size: " + contentChunk.length());
                }
            }
        } else {
            System.out.println("Received chunk for document: " + documentId + ", chunk size: " + chunk.length());
        }
    }
}