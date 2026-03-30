package com.TsukasaChan.ShopVault.service.system.impl;

import com.TsukasaChan.ShopVault.entity.system.MessagePush;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.mapper.system.MessagePushMapper;
import com.TsukasaChan.ShopVault.service.system.MessagePushService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.TsukasaChan.ShopVault.websocket.WebSocketMessage;
import com.TsukasaChan.ShopVault.websocket.WebSocketService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePushServiceImpl extends ServiceImpl<MessagePushMapper, MessagePush> implements MessagePushService {

    private final WebSocketService webSocketService;
    private final UserService userService;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void pushToUser(Long userId, String type, String title, String content) {
        MessagePush message = createMessage(type, title, content);
        message.setTargetUserId(userId);
        save(message);

        try {
            WebSocketMessage<String> wsMessage = WebSocketMessage.of(type, title, content, content);
            webSocketService.sendToUser(userId, "/queue/messages", wsMessage);
            
            message.setStatus(MessagePush.STATUS_SENT);
            message.setSendTime(LocalDateTime.now());
            updateById(message);
            
            log.info("消息推送成功: userId={}, type={}", userId, type);
        } catch (Exception e) {
            handlePushError(message, e);
        }
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void pushToRole(String role, String type, String title, String content) {
        List<User> users = userService.list(new LambdaQueryWrapper<User>().eq(User::getRole, role));

        for (User user : users) {
            pushToUser(user.getId(), type, title, content);
        }
        
        log.info("角色消息推送完成: role={}, userCount={}", role, users.size());
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void pushToAll(String type, String title, String content) {
        MessagePush message = createMessage(type, title, content);
        message.setTargetRole("ALL");
        save(message);

        try {
            WebSocketMessage<String> wsMessage = WebSocketMessage.of(type, title, content, content);
            webSocketService.broadcast("/topic/messages", wsMessage);
            
            message.setStatus(MessagePush.STATUS_SENT);
            message.setSendTime(LocalDateTime.now());
            updateById(message);
            
            log.info("全量消息推送成功: type={}", type);
        } catch (Exception e) {
            handlePushError(message, e);
        }
    }

    @Override
    public List<MessagePush> getUserMessages(Long userId) {
        return list(new LambdaQueryWrapper<MessagePush>()
                .eq(MessagePush::getTargetUserId, userId)
                .or()
                .eq(MessagePush::getTargetRole, "ALL")
                .orderByDesc(MessagePush::getSendTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long messageId) {
        MessagePush message = getById(messageId);
        if (message != null && message.getStatus() != MessagePush.STATUS_READ) {
            message.setStatus(MessagePush.STATUS_READ);
            message.setReadTime(LocalDateTime.now());
            updateById(message);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryFailedMessages() {
        List<MessagePush> failedMessages = list(new LambdaQueryWrapper<MessagePush>()
                .eq(MessagePush::getStatus, MessagePush.STATUS_FAILED)
                .lt(MessagePush::getRetryCount, 3));

        for (MessagePush message : failedMessages) {
            if (message.getTargetUserId() != null) {
                pushToUser(message.getTargetUserId(), message.getType(), 
                        message.getTitle(), message.getContent());
            } else {
                pushToAll(message.getType(), message.getTitle(), message.getContent());
            }
            removeById(message.getId());
        }

        log.info("重试失败消息完成: count={}", failedMessages.size());
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void pushToAdmin(String type, String title, String content, String linkUrl, Long relatedId) {
        MessagePush message = createMessage(type, title, content);
        message.setTargetRole("ADMIN");
        message.setLinkUrl(linkUrl);
        message.setRelatedId(relatedId);
        save(message);

        try {
            WebSocketMessage<String> wsMessage = WebSocketMessage.of(type, title, content, content);
            webSocketService.broadcast("/topic/admin-messages", wsMessage);
            
            message.setStatus(MessagePush.STATUS_SENT);
            message.setSendTime(LocalDateTime.now());
            updateById(message);
            
            log.info("管理员消息推送成功: type={}, relatedId={}", type, relatedId);
        } catch (Exception e) {
            handlePushError(message, e);
        }
    }

    @Override
    public List<MessagePush> getAdminMessages() {
        return list(new LambdaQueryWrapper<MessagePush>()
                .eq(MessagePush::getTargetRole, "ADMIN")
                .in(MessagePush::getType, 
                    MessagePush.TYPE_ADMIN_PURCHASE,
                    MessagePush.TYPE_ADMIN_AFTER_SALES,
                    MessagePush.TYPE_ADMIN_COMMENT,
                    MessagePush.TYPE_ADMIN_CHAT)
                .orderByDesc(MessagePush::getSendTime)
                .last("LIMIT 100"));
    }

    @Override
    public int getAdminUnreadCount() {
        return (int) count(new LambdaQueryWrapper<MessagePush>()
                .eq(MessagePush::getTargetRole, "ADMIN")
                .eq(MessagePush::getStatus, MessagePush.STATUS_SENT)
                .in(MessagePush::getType,
                    MessagePush.TYPE_ADMIN_PURCHASE,
                    MessagePush.TYPE_ADMIN_AFTER_SALES,
                    MessagePush.TYPE_ADMIN_COMMENT,
                    MessagePush.TYPE_ADMIN_CHAT));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsReadForAdmin() {
        List<MessagePush> unreadMessages = list(new LambdaQueryWrapper<MessagePush>()
                .eq(MessagePush::getTargetRole, "ADMIN")
                .eq(MessagePush::getStatus, MessagePush.STATUS_SENT)
                .in(MessagePush::getType,
                    MessagePush.TYPE_ADMIN_PURCHASE,
                    MessagePush.TYPE_ADMIN_AFTER_SALES,
                    MessagePush.TYPE_ADMIN_COMMENT,
                    MessagePush.TYPE_ADMIN_CHAT));
        
        LocalDateTime now = LocalDateTime.now();
        for (MessagePush message : unreadMessages) {
            message.setStatus(MessagePush.STATUS_READ);
            message.setReadTime(now);
        }
        updateBatchById(unreadMessages);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsReadByRelatedId(String type, Long relatedId) {
        MessagePush message = getOne(new LambdaQueryWrapper<MessagePush>()
                .eq(MessagePush::getType, type)
                .eq(MessagePush::getRelatedId, relatedId)
                .eq(MessagePush::getStatus, MessagePush.STATUS_SENT));
        
        if (message != null) {
            message.setStatus(MessagePush.STATUS_READ);
            message.setReadTime(LocalDateTime.now());
            updateById(message);
        }
    }

    private MessagePush createMessage(String type, String title, String content) {
        MessagePush message = new MessagePush();
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setStatus(MessagePush.STATUS_PENDING);
        message.setRetryCount(0);
        return message;
    }

    private void handlePushError(MessagePush message, Exception e) {
        log.error("消息推送失败: id={}, error={}", message.getId(), e.getMessage());
        message.setStatus(MessagePush.STATUS_FAILED);
        message.setRetryCount(message.getRetryCount() + 1);
        message.setErrorMessage(e.getMessage());
        updateById(message);
    }
}
