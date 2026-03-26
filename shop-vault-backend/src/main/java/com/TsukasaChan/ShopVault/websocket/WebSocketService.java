package com.TsukasaChan.ShopVault.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketEventListener webSocketEventListener;

    public void broadcast(String destination, Object message) {
        messagingTemplate.convertAndSend(Objects.requireNonNull(destination), Objects.requireNonNull(message));
        log.debug("广播消息: {}", message);
    }

    public void sendToUser(Long userId, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(
                Objects.requireNonNull(String.valueOf(userId)), 
                Objects.requireNonNull(destination), 
                Objects.requireNonNull(message));
        log.debug("发送消息给用户 {}: {}", userId, message);
    }

    public void notifyStockWarning(Long productId, String productName, Integer stock) {
        WebSocketMessage<?> message = WebSocketMessage.stockWarning(productId, productName, stock);
        broadcast("/topic/stock-warning", message);
    }

    public void notifyUser(Long userId, String type, String title, String content, Object data) {
        WebSocketMessage<?> message = WebSocketMessage.of(type, title, content, data);
        sendToUser(userId, "/queue/notifications", message);
    }

    public boolean isUserOnline(Long userId) {
        return webSocketEventListener.isUserOnline(userId);
    }
}
