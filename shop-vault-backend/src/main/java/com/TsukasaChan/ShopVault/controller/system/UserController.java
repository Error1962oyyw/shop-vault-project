package com.TsukasaChan.ShopVault.controller.system;

import com.TsukasaChan.ShopVault.annotation.LogOperation;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.dto.PasswordUpdateDto;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.integration.LocalFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final LocalFileService localFileService;

    @GetMapping("/profile")
    public Result<User> getProfile() {
        User user = getCurrentUser();
        user.setPassword(null);
        return Result.success(user);
    }

    @LogOperation(module = "个人中心", action = "修改个人资料")
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody User updateInfo) {
        userService.updateProfile(getCurrentUserId(), updateInfo);
        return Result.success("资料修改成功");
    }

    @LogOperation(module = "个人中心", action = "修改密码")
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody PasswordUpdateDto dto) {
        userService.updatePassword(getCurrentUserId(), dto.getOldPassword(), dto.getNewPassword());
        return Result.success("密码修改成功，请牢记新密码");
    }

    @LogOperation(module = "个人中心", action = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只能上传图片文件");
        }

        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Result.error(400, "图片大小不能超过2MB");
        }

        String avatarUrl = localFileService.uploadAvatar(file, getCurrentUserId());
        
        User updateInfo = new User();
        updateInfo.setAvatar(avatarUrl);
        userService.updateProfile(getCurrentUserId(), updateInfo);

        return Result.success(avatarUrl);
    }
}
