package com.TsukasaChan.ShopVault.controller.system;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.entity.system.ChatMessage;
import com.TsukasaChan.ShopVault.integration.LocalFileService;
import com.TsukasaChan.ShopVault.service.system.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController extends BaseController {

    private final ChatMessageService chatMessageService;
    private final LocalFileService localFileService;

    @PostMapping("/send")
    public Result<String> sendToAdmin(@RequestBody ChatMessage msg) {
        chatMessageService.sendToAdmin(getCurrentUserId(), msg);
        return Result.success("发送成功");
    }

    @PostMapping("/send-image")
    public Result<String> sendImageToAdmin(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只能上传图片文件");
        }

        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Result.error(400, "图片大小不能超过5MB");
        }

        String imageUrl = localFileService.uploadFile(file, "chat");
        
        ChatMessage msg = new ChatMessage();
        msg.setContent(imageUrl);
        msg.setMsgType(2);
        chatMessageService.sendToAdmin(getCurrentUserId(), msg);
        
        return Result.success(imageUrl);
    }

    @GetMapping("/history")
    public Result<List<ChatMessage>> getUserHistory() {
        return Result.success(chatMessageService.getUserHistory(getCurrentUserId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/reply")
    public Result<String> replyToUser(@RequestBody ChatMessage msg) {
        chatMessageService.replyToUser(getCurrentUserId(), msg);
        return Result.success("回复成功");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/reply-image")
    public Result<String> replyImageToUser(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只能上传图片文件");
        }

        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Result.error(400, "图片大小不能超过5MB");
        }

        String imageUrl = localFileService.uploadFile(file, "chat");
        
        ChatMessage msg = new ChatMessage();
        msg.setContent(imageUrl);
        msg.setMsgType(2);
        msg.setReceiverId(userId);
        chatMessageService.replyToUser(getCurrentUserId(), msg);
        
        return Result.success(imageUrl);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/history/{userId}")
    public Result<List<ChatMessage>> getAdminHistory(@PathVariable Long userId) {
        return Result.success(chatMessageService.getAdminHistory(getCurrentUserId(), userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public Result<List<Map<String, Object>>> getChatUsers() {
        return Result.success(chatMessageService.getChatUsers());
    }
}
