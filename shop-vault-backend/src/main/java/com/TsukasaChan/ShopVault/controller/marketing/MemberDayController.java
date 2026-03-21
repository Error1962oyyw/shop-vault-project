package com.TsukasaChan.ShopVault.controller.marketing;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.dto.MemberDayDto;
import com.TsukasaChan.ShopVault.entity.marketing.Activity;
import com.TsukasaChan.ShopVault.service.marketing.MemberDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-day")
@RequiredArgsConstructor
public class MemberDayController extends BaseController {

    private final MemberDayService memberDayService;

    @GetMapping("/current")
    public Result<MemberDayDto> getCurrentMemberDay() {
        Activity activity = memberDayService.getCurrentMemberDay();
        if (activity == null) {
            return Result.success(null);
        }
        return Result.success(memberDayService.getMemberDayInfo(activity.getId()));
    }

    @GetMapping("/upcoming")
    public Result<List<Activity>> getUpcomingMemberDays() {
        return Result.success(memberDayService.getUpcomingMemberDays());
    }

    @GetMapping("/info/{id}")
    public Result<MemberDayDto> getMemberDayInfo(@PathVariable Long id) {
        MemberDayDto dto = memberDayService.getMemberDayInfo(id);
        if (dto == null) {
            return Result.error(404, "会员日活动不存在");
        }
        return Result.success(dto);
    }

    @GetMapping("/check")
    public Result<Boolean> isMemberDayToday() {
        return Result.success(memberDayService.isMemberDayToday());
    }
}
