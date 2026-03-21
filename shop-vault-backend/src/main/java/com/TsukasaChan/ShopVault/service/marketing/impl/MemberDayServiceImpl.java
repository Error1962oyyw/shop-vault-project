package com.TsukasaChan.ShopVault.service.marketing.impl;

import com.TsukasaChan.ShopVault.dto.MemberDayDto;
import com.TsukasaChan.ShopVault.entity.marketing.Activity;
import com.TsukasaChan.ShopVault.mapper.marketing.ActivityMapper;
import com.TsukasaChan.ShopVault.service.marketing.MemberDayService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MemberDayServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements MemberDayService {

    @Override
    public Activity getCurrentMemberDay() {
        LocalDateTime now = LocalDateTime.now();
        List<Activity> activities = list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getType, 1)
                .eq(Activity::getStatus, 1)
                .le(Activity::getStartTime, now)
                .ge(Activity::getEndTime, now));

        for (Activity activity : activities) {
            if (isRuleMatch(activity)) {
                return activity;
            }
        }
        return null;
    }

    @Override
    public List<Activity> getUpcomingMemberDays() {
        LocalDateTime now = LocalDateTime.now();
        return list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getType, 1)
                .eq(Activity::getStatus, 1)
                .ge(Activity::getStartTime, now)
                .orderByAsc(Activity::getStartTime));
    }

    @Override
    public MemberDayDto getMemberDayInfo(Long activityId) {
        Activity activity = getById(activityId);
        if (activity == null) {
            return null;
        }

        MemberDayDto dto = new MemberDayDto();
        dto.setId(activity.getId());
        dto.setName(activity.getName());
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        dto.setDiscountRate(activity.getDiscountRate());
        dto.setPointsMultiplier(activity.getPointsMultiplier());
        dto.setRuleExpression(activity.getRuleExpression());
        dto.setIsActive(isRuleMatch(activity));

        StringBuilder desc = new StringBuilder();
        if (activity.getDiscountRate() != null) {
            int discount = BigDecimal.ONE.subtract(activity.getDiscountRate()).multiply(new BigDecimal("100")).intValue();
            desc.append("全场").append(discount).append("折优惠；");
        }
        if (activity.getPointsMultiplier() != null && activity.getPointsMultiplier().compareTo(BigDecimal.ONE) > 0) {
            desc.append("购物积分").append(activity.getPointsMultiplier().intValue()).append("倍；");
        }
        dto.setDescription(desc.toString());

        return dto;
    }

    @Override
    public boolean isMemberDayToday() {
        return getCurrentMemberDay() != null;
    }

    @Override
    public BigDecimal applyMemberDayDiscount(BigDecimal originalAmount) {
        Activity memberDay = getCurrentMemberDay();
        if (memberDay != null && memberDay.getDiscountRate() != null) {
            return originalAmount.multiply(memberDay.getDiscountRate()).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return originalAmount;
    }

    @Override
    public BigDecimal getMemberDayPointsMultiplier() {
        Activity memberDay = getCurrentMemberDay();
        if (memberDay != null && memberDay.getPointsMultiplier() != null) {
            return memberDay.getPointsMultiplier();
        }
        return BigDecimal.ONE;
    }

    private boolean isRuleMatch(Activity activity) {
        String rule = activity.getRuleExpression();
        if (rule == null) {
            return true;
        }

        LocalDate today = LocalDate.now();
        
        if ("EVERYDAY".equalsIgnoreCase(rule)) {
            return true;
        }

        if ("WEEKLY".equalsIgnoreCase(rule)) {
            return true;
        }

        try {
            int dayOfMonth = Integer.parseInt(rule);
            return today.getDayOfMonth() == dayOfMonth;
        } catch (NumberFormatException e) {
            if (rule.startsWith("WEEK_")) {
                String dayName = rule.substring(5);
                try {
                    DayOfWeek targetDay = DayOfWeek.valueOf(dayName.toUpperCase());
                    return today.getDayOfWeek() == targetDay;
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return false;
    }
}
