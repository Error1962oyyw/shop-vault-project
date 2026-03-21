package com.TsukasaChan.ShopVault.websocket;

import com.TsukasaChan.ShopVault.security.JwtUtils;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.TsukasaChan.ShopVault.entity.system.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final WebSocketEventListener webSocketEventListener;
    private final UserService userService;

    @Override
    public @NonNull Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);

            if (StringUtils.hasText(token)) {
                try {
                    String username = jwtUtils.extractUsername(token);
                    
                    if (StringUtils.hasText(username) && jwtUtils.validateToken(token, username)) {
                        User user = userService.getByUsername(username);
                        
                        if (user != null) {
                            Long userId = user.getId();
                            String sessionId = accessor.getSessionId();
                            webSocketEventListener.registerSession(sessionId, userId);

                            accessor.setUser(new WebSocketPrincipal(userId));
                            log.info("WebSocket用户认证成功: userId={}, sessionId={}", userId, sessionId);
                        }
                    }
                } catch (Exception e) {
                    log.warn("WebSocket连接认证失败: {}", e.getMessage());
                }
            } else {
                log.debug("WebSocket连接无token，允许匿名连接");
            }
        }

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return accessor.getFirstNativeHeader("token");
    }
}
