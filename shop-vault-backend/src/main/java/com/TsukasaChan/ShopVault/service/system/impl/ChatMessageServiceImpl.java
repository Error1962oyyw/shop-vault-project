package com.TsukasaChan.ShopVault.service.system.impl;

import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.TsukasaChan.ShopVault.entity.system.ChatMessage;
import com.TsukasaChan.ShopVault.service.system.ChatMessageService;
import com.TsukasaChan.ShopVault.mapper.system.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    private final UserService userService;

    public List<ChatMessage> fetchHistoryAndMarkRead(Long myId, Long peerId, Long unreadSenderId, Long unreadReceiverId) {
        List<ChatMessage> history = list(new LambdaQueryWrapper<ChatMessage>()
                .and(wrapper -> wrapper.eq(ChatMessage::getSenderId, myId).eq(ChatMessage::getReceiverId, peerId)
                        .or()
                        .eq(ChatMessage::getSenderId, peerId).eq(ChatMessage::getReceiverId, myId))
                .orderByAsc(ChatMessage::getCreateTime));

        this.update(new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getSenderId, unreadSenderId)
                .eq(ChatMessage::getReceiverId, unreadReceiverId)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1));

        return history;
    }

    private Long getAdminId() {
        User admin = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN").last("LIMIT 1"));
        if (admin == null) throw new RuntimeException("系统暂无客服人员");
        return admin.getId();
    }

    @Override
    public void sendToAdmin(Long userId, ChatMessage msg) {
        msg.setSenderId(userId);
        msg.setReceiverId(getAdminId());
        msg.setIsRead(0);
        if (msg.getMsgType() == null) msg.setMsgType(1);
        this.save(msg);
    }

    @Override
    public List<ChatMessage> getUserHistory(Long userId) {
        Long adminId = getAdminId();
        return this.fetchHistoryAndMarkRead(userId, adminId, adminId, userId);
    }

    @Override
    public void replyToUser(Long adminId, ChatMessage msg) {
        if (msg.getReceiverId() == null) throw new RuntimeException("必须指定回复的用户ID");
        msg.setSenderId(adminId);
        msg.setIsRead(0);
        if (msg.getMsgType() == null) msg.setMsgType(1);
        this.save(msg);
    }

    @Override
    public List<ChatMessage> getAdminHistory(Long adminId, Long userId) {
        return this.fetchHistoryAndMarkRead(adminId, userId, userId, adminId);
    }

    @Override
    public List<Map<String, Object>> getChatUsers() {
        Long adminId = getAdminId();
        
        List<ChatMessage> messages = list(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getReceiverId, adminId)
                .or()
                .eq(ChatMessage::getSenderId, adminId)
                .orderByDesc(ChatMessage::getCreateTime));

        Set<Long> userIds = messages.stream()
                .map(msg -> msg.getSenderId().equals(adminId) ? msg.getReceiverId() : msg.getSenderId())
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> users = userService.list(new LambdaQueryWrapper<User>()
                .in(User::getId, userIds));

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, ChatMessage> lastMessageMap = new LinkedHashMap<>();
        for (ChatMessage msg : messages) {
            Long otherId = msg.getSenderId().equals(adminId) ? msg.getReceiverId() : msg.getSenderId();
            if (!lastMessageMap.containsKey(otherId)) {
                lastMessageMap.put(otherId, msg);
            }
        }

        long unreadCount = messages.stream()
                .filter(m -> m.getSenderId().equals(adminId) ? false : m.getIsRead() == 0)
                .count();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, ChatMessage> entry : lastMessageMap.entrySet()) {
            Long userId = entry.getKey();
            ChatMessage lastMsg = entry.getValue();
            User user = userMap.get(userId);
            
            if (user != null) {
                Map<String, Object> userInfo = new LinkedHashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                userInfo.put("lastMessage", lastMsg.getContent());
                userInfo.put("lastMessageTime", lastMsg.getCreateTime());
                
                long userUnreadCount = messages.stream()
                        .filter(m -> m.getSenderId().equals(userId) && m.getIsRead() == 0)
                        .count();
                userInfo.put("unreadCount", userUnreadCount);
                
                result.add(userInfo);
            }
        }

        return result;
    }
}
