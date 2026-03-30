package com.TsukasaChan.ShopVault.service.system;

import com.TsukasaChan.ShopVault.entity.system.MessagePush;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface MessagePushService extends IService<MessagePush> {

    void pushToUser(Long userId, String type, String title, String content);

    void pushToRole(String role, String type, String title, String content);

    void pushToAll(String type, String title, String content);

    List<MessagePush> getUserMessages(Long userId);

    void markAsRead(Long messageId);

    void retryFailedMessages();

    void pushToAdmin(String type, String title, String content, String linkUrl, Long relatedId);

    List<MessagePush> getAdminMessages();

    int getAdminUnreadCount();

    void markAllAsReadForAdmin();

    void markAsReadByRelatedId(String type, Long relatedId);
}
