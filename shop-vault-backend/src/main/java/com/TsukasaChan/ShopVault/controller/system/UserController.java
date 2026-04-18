package com.TsukasaChan.ShopVault.controller.system;

import com.TsukasaChan.ShopVault.annotation.LogOperation;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.dto.PasswordUpdateDto;
import com.TsukasaChan.ShopVault.dto.UpdateProfileDto;
import com.TsukasaChan.ShopVault.entity.marketing.BalanceRecord;
import com.TsukasaChan.ShopVault.entity.marketing.UserVipInfo;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.integration.LocalFileService;
import com.TsukasaChan.ShopVault.service.marketing.BalanceRecordService;
import com.TsukasaChan.ShopVault.service.marketing.UserVipInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final LocalFileService localFileService;
    private final BalanceRecordService balanceRecordService;
    private final UserVipInfoService userVipInfoService;

    @GetMapping("/profile")
    public Result<User> getProfile() {
        User user = getCurrentUser();
        user.setPassword(null);
        UserVipInfo vipInfo = userVipInfoService.getByUserId(user.getId());
        user.setMemberLevel(vipInfo != null ? vipInfo.getVipLevel() : UserVipInfo.LEVEL_NORMAL);
        return Result.success(user);
    }

    @LogOperation(module = "个人中心", action = "修改个人资料")
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody UpdateProfileDto dto) {
        User updateInfo = new User();
        updateInfo.setNickname(dto.getNickname());
        updateInfo.setPhone(dto.getPhone());
        updateInfo.setAvatar(dto.getAvatar());
        updateInfo.setGender(dto.getGender());
        updateInfo.setBirthday(dto.getBirthday());
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

    @LogOperation(module = "个人中心", action = "模拟充值")
    @PostMapping("/balance/recharge")
    public Result<Map<String, Object>> rechargeBalance(@RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error(400, "充值金额必须大于0");
        }
        
        if (amount.scale() > 2) {
            return Result.error(400, "充值金额最多支持两位小数");
        }
        
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            return Result.error(400, "单次充值金额不能超过10000元");
        }
        
        Long userId = getCurrentUserId();
        
        boolean updated = userService.updateBalanceWithLock(userId, amount);
        if (!updated) {
            return Result.error(500, "充值失败，请重试");
        }
        
        User user = userService.getById(userId);
        BigDecimal balanceBefore = user.getBalance().subtract(amount);
        BigDecimal balanceAfter = user.getBalance();
        
        balanceRecordService.recordBalanceChange(
                userId,
                amount,
                balanceBefore,
                balanceAfter,
                BalanceRecord.TYPE_RECHARGE,
                "模拟充值",
                null
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("balance", balanceAfter);
        result.put("amount", amount);
        return Result.success(result);
    }

    @GetMapping("/balance/records")
    public Result<?> getBalanceRecords() {
        return Result.success(balanceRecordService.getUserBalanceRecords(getCurrentUserId()));
    }
}
