package com.TsukasaChan.ShopVault.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketEventListener {

    private final Map<String, Long> sessionUserMap;

    public WebSocketEventListener() {
        this.sessionUserMap = new ConcurrentHashMap<>();
        log.info("WebSocketEventListener initialized, sessionUserMap ready");
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (sessionId != null) {
            log.info("WebSocket连接建立: {}", sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (sessionId != null) {
            Long removedUserId = sessionUserMap.remove(sessionId);
            if (removedUserId != null) {
                log.info("WebSocket连接断开: sessionId={}, userId={}", sessionId, removedUserId);
            } else {
                log.debug("WebSocket连接断开(未注册用户): sessionId={}", sessionId);
            }
        }
    }

    public void registerSession(String sessionId, Long userId) {
        Objects.requireNonNull(sessionId, "sessionId cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        sessionUserMap.put(sessionId, userId);
        log.debug("注册会话映射: sessionId={}, userId={}", sessionId, userId);
    }

    public int getOnlineCount() {
        return sessionUserMap.size();
    }

    public boolean isUserOnline(Long userId) {
        return sessionUserMap.containsValue(userId);
    }
}
