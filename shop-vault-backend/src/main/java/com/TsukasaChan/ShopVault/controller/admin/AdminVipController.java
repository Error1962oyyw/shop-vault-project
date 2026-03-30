package com.TsukasaChan.ShopVault.controller.admin;

import com.TsukasaChan.ShopVault.common.PageResult;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.common.ServiceUtils;
import com.TsukasaChan.ShopVault.entity.marketing.UserVipInfo;
import com.TsukasaChan.ShopVault.entity.marketing.VipMembership;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.service.marketing.UserVipInfoService;
import com.TsukasaChan.ShopVault.service.marketing.VipMembershipService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/vip")
@RequiredArgsConstructor
public class AdminVipController {

    private final UserVipInfoService userVipInfoService;
    private final VipMembershipService vipMembershipService;
    private final UserService userService;

    @GetMapping("/users")
    public Result<PageResult<UserVipInfo>> getVipUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer vipLevel) {
        PageResult<UserVipInfo> result = ServiceUtils.queryPage(userVipInfoService, pageNum, pageSize, wrapper -> {
            if (vipLevel != null) {
                wrapper.eq(UserVipInfo::getVipLevel, vipLevel);
            }
            wrapper.orderByDesc(UserVipInfo::getVipLevel);
        });
        
        Set<Long> userIds = result.getRecords().stream()
                .map(UserVipInfo::getUserId)
                .collect(Collectors.toSet());
        
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            
            result.getRecords().forEach(vipInfo -> {
                User user = userMap.get(vipInfo.getUserId());
                if (user != null) {
                    vipInfo.setUsername(user.getUsername());
                    vipInfo.setNickname(user.getNickname());
                    vipInfo.setAvatar(user.getAvatar());
                }
            });
        }
        
        return Result.success(result);
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
    public Result<String> updateVipLevel(
            @PathVariable Long userId, 
            @RequestParam Integer level,
            @RequestParam(required = false) Integer days) {
        userVipInfoService.updateVipLevel(userId, level, days);
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
