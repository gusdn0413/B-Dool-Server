//package com.bdool.documentservice.config.handler;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//@Component
//public class WebSocketHandler extends TextWebSocketHandler {
//
//    private final ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        String sessionId = getSessionIdFromUri(session.getUri().toString());
//        sessionMap.putIfAbsent(sessionId, new CopyOnWriteArrayList<>());
//        sessionMap.get(sessionId).add(session);
//        System.out.println("Session established: " + sessionId); // 추가된 로그
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String sessionId = getSessionIdFromUri(session.getUri().toString());
//        for (WebSocketSession s : sessionMap.get(sessionId)) {
//            if (s.isOpen() && !s.getId().equals(session.getId())) {
//                s.sendMessage(new TextMessage(message.getPayload()));  // 다른 클라이언트로 메시지 전송
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        String sessionId = getSessionIdFromUri(session.getUri().toString());
//        sessionMap.get(sessionId).remove(session);
//        System.out.println("Session closed: " + sessionId); // 추가된 로그
//    }
//
//    private String getSessionIdFromUri(String uri) {
//        return uri.split("/")[3];  // 예시로 URI에서 세션 ID를 추출
//    }
//}