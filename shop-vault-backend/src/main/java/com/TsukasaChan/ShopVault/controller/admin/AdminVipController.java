package com.TsukasaChan.ShopVault.controller.admin;

import com.TsukasaChan.ShopVault.common.PageResult;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.common.ServiceUtils;
import com.TsukasaChan.ShopVault.entity.marketing.UserVipInfo;
import com.TsukasaChan.ShopVault.entity.marketing.VipMembership;
import com.TsukasaChan.ShopVault.service.marketing.UserVipInfoService;
import com.TsukasaChan.ShopVault.service.marketing.VipMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vip")
@RequiredArgsConstructor
public class AdminVipController {

    private final UserVipInfoService userVipInfoService;
    private final VipMembershipService vipMembershipService;

    @GetMapping("/users")
    public Result<PageResult<UserVipInfo>> getVipUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer vipLevel) {
        return Result.success(ServiceUtils.queryPage(userVipInfoService, pageNum, pageSize, wrapper -> {
            if (vipLevel != null) {
                wrapper.eq(UserVipInfo::getVipLevel, vipLevel);
            }
            wrapper.orderByDesc(UserVipInfo::getVipLevel);
        }));
    }

    @GetMapping("/users/{userId}")
    public Result<UserVipInfo> getUserVipInfo(@PathVariable Long userId) {
        return Result.success(userVipInfoService.getByUserId(userId));
    }

    @PutMapping("/users/{userId}/extend")
    public Result<String> extendVip(@PathVariable Long userId, @RequestParam Integer days) {
        userVipInfoService.extendVip(userId, days);
        return Result.success("延期成功");
    }

    @PutMapping("/users/{userId}/level")
    public Result<String> updateVipLevel(@PathVariable Long userId, @RequestParam Integer level) {
        UserVipInfo vipInfo = userVipInfoService.getByUserId(userId);
        if (vipInfo != null) {
            vipInfo.setVipLevel(level);
            userVipInfoService.updateById(vipInfo);
        }
        return Result.success("等级更新成功");
    }

    @GetMapping("/memberships")
    public Result<List<VipMembership>> getVipMemberships(@RequestParam(required = false) Long userId) {
        return Result.success(vipMembershipService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VipMembership>()
                        .eq(userId != null, VipMembership::getUserId, userId)
                        .orderByDesc(VipMembership::getCreateTime)));
    }
}
