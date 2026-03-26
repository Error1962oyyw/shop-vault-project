package com.TsukasaChan.ShopVault.controller.marketing;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.dto.ExchangeVipDto;
import com.TsukasaChan.ShopVault.dto.VipInfoDto;
import com.TsukasaChan.ShopVault.entity.marketing.UserVipInfo;
import com.TsukasaChan.ShopVault.entity.marketing.VipMembership;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.service.marketing.UserVipInfoService;
import com.TsukasaChan.ShopVault.service.marketing.VipMembershipService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/vip")
@RequiredArgsConstructor
public class VipController {

    private final VipMembershipService vipMembershipService;
    private final UserVipInfoService userVipInfoService;
    private final UserService userService;

    @GetMapping("/info")
    public Result<VipInfoDto> getVipInfo(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByUsername(userDetails.getUsername());
        UserVipInfo vipInfo = userVipInfoService.getByUserId(user.getId());
        
        VipInfoDto dto = new VipInfoDto();
        dto.setVipLevel(vipInfo.getVipLevel());
        dto.setDiscountRate(vipInfo.getDiscountRate());
        dto.setVipExpireTime(vipInfo.getVipExpireTime());
        dto.setTotalVipDays(vipInfo.getTotalVipDays());
        
        boolean isVip = vipInfo.getVipExpireTime() != null && 
                        vipInfo.getVipExpireTime().isAfter(LocalDateTime.now());
        dto.setIsVip(isVip);
        
        if (isVip) {
            long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), vipInfo.getVipExpireTime());
            dto.setRemainingDays((int) remainingDays);
        } else {
            dto.setRemainingDays(0);
        }
        
        return Result.success(dto);
    }

    @PostMapping("/exchange")
    public Result<String> exchangeVip(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ExchangeVipDto dto) {
        User user = userService.getByUsername(userDetails.getUsername());
        vipMembershipService.exchangeVip(user.getId(), dto.getVipType());
        return Result.success("VIP兑换成功");
    }

    @GetMapping("/history")
    public Result<List<VipMembership>> getVipHistory(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByUsername(userDetails.getUsername());
        List<VipMembership> history = vipMembershipService.getVipHistory(user.getId());
        return Result.success(history);
    }

    @GetMapping("/check")
    public Result<Boolean> checkVip(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByUsername(userDetails.getUsername());
        boolean isVip = vipMembershipService.isVip(user.getId());
        return Result.success(isVip);
    }
}
