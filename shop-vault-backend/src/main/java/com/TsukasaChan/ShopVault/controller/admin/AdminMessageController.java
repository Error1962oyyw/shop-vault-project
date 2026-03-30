package com.TsukasaChan.ShopVault.controller.admin;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.entity.system.MessagePush;
import com.TsukasaChan.ShopVault.service.system.MessagePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMessageController {

    private final MessagePushService messagePushService;

    @GetMapping
    public Result<List<MessagePush>> getAdminMessages() {
        return Result.success(messagePushService.getAdminMessages());
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount() {
        Map<String, Integer> result = new HashMap<>();
        result.put("count", messagePushService.getAdminUnreadCount());
        return Result.success(result);
    }

    @PostMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id) {
        messagePushService.markAsRead(id);
        return Result.success("已标记为已读");
    }

    @PostMapping("/read-all")
    public Result<String> markAllAsRead() {
        messagePushService.markAllAsReadForAdmin();
        return Result.success("全部已标记为已读");
    }
}
