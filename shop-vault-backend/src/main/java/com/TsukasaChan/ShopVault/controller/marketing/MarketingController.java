package com.TsukasaChan.ShopVault.controller.marketing;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
import com.TsukasaChan.ShopVault.service.marketing.impl.PointsRecordServiceImpl.SignInResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketing")
@RequiredArgsConstructor
public class MarketingController extends BaseController {

    private final PointsRecordService pointsRecordService;

    @Data
    public static class SignInStatus {
        private boolean todaySigned;
        private Integer todayCount;
        private Integer consecutiveDays;
        private Integer dailyLimit;
        private Integer remaining;
    }

    @PostMapping("/sign-in")
    public Result<SignInResult> signIn() {
        Long userId = getCurrentUserId();
        SignInResult result = pointsRecordService.signIn(userId);
        return Result.success(result);
    }

    @GetMapping("/points/records")
    public Result<List<PointsRecord>> getPointsRecords() {
        Long userId = getCurrentUserId();
        List<PointsRecord> records = pointsRecordService.list(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getUserId, userId)
                .orderByDesc(PointsRecord::getCreateTime));
        return Result.success(records);
    }

    @GetMapping("/points/today-signed")
    public Result<Boolean> todaySigned() {
        Long userId = getCurrentUserId();
        boolean signed = pointsRecordService.todaySigned(userId);
        return Result.success(signed);
    }

    @GetMapping("/sign-in/status")
    public Result<SignInStatus> getSignInStatus() {
        Long userId = getCurrentUserId();
        SignInStatus status = new SignInStatus();
        status.setTodaySigned(pointsRecordService.todaySigned(userId));
        status.setTodayCount(pointsRecordService.getTodaySignInCount(userId));
        status.setConsecutiveDays(pointsRecordService.getConsecutiveSignInDays(userId));
        return Result.success(status);
    }
}
