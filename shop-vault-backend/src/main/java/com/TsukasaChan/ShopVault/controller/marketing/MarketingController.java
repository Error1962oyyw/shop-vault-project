package com.TsukasaChan.ShopVault.controller.marketing;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
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
    public static class SignInResult {
        private Integer points;
        private String message;
        
        public SignInResult(Integer points, String message) {
            this.points = points;
            this.message = message;
        }
    }

    @PostMapping("/sign-in")
    public Result<SignInResult> signIn() {
        Long userId = getCurrentUserId();
        Integer points = pointsRecordService.signIn(userId);
        return Result.success(new SignInResult(points, "签到成功"));
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
}
