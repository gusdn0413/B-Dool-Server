package com.bdool.documentservice.controller;

import com.bdool.documentservice.entity.DocumentUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DocumentMessageController {

    private final Logger log = LoggerFactory.getLogger(DocumentMessageController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    @MessageMapping("/editDocument")
    public void sendDocumentUpdate(DocumentUpdateMessage message) {
        Long documentId = message.getDocumentId();
        String content = message.getContent();

        if (documentId == null || content == null) {
            log.error("Received null documentId or content.");
            return;
        }

        redisTemplate.opsForValue().set("document:" + documentId, content);
        log.info("Document with ID {} stored in Redis", documentId);

        messagingTemplate.convertAndSend("/topic/document/" + documentId, new DocumentUpdateMessage(documentId, content));
        log.info("Document with ID {} sent to topic", documentId);
    }
}