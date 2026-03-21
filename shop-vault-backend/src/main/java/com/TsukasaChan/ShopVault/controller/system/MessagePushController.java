package com.TsukasaChan.ShopVault.controller.system;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.entity.system.MessagePush;
import com.TsukasaChan.ShopVault.service.system.MessagePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessagePushController extends BaseController {

    private final MessagePushService messagePushService;

    @GetMapping("/my")
    public Result<List<MessagePush>> getMyMessages() {
        return Result.success(messagePushService.getUserMessages(getCurrentUserId()));
    }

    @PostMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id) {
        messagePushService.markAsRead(id);
        return Result.success("已标记为已读");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/push/user/{userId}")
    public Result<String> pushToUser(
            @PathVariable Long userId,
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam String content) {
        messagePushService.pushToUser(userId, type, title, content);
        return Result.success("消息推送成功");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/push/role/{role}")
    public Result<String> pushToRole(
            @PathVariable String role,
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam String content) {
        messagePushService.pushToRole(role, type, title, content);
        return Result.success("角色消息推送成功");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/push/all")
    public Result<String> pushToAll(
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam String content) {
        messagePushService.pushToAll(type, title, content);
        return Result.success("全量消息推送成功");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/retry-failed")
    public Result<String> retryFailedMessages() {
        messagePushService.retryFailedMessages();
        return Result.success("失败消息重试完成");
    }
}
