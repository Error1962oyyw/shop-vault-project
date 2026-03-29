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
        if (rule == null || rule.isEmpty()) {
            return true;
        }

        LocalDate today = LocalDate.now();
        
        String[] parts = rule.split(";");
        boolean hasWeekRule = false;
        boolean hasMonthRule = false;
        boolean weekMatch = false;
        boolean monthMatch = false;
        
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("week:")) {
                hasWeekRule = true;
                String daysStr = part.substring(5);
                if (!daysStr.isEmpty()) {
                    String[] days = daysStr.split(",");
                    int todayDayOfWeek = today.getDayOfWeek().getValue();
                    for (String day : days) {
                        try {
                            int targetDay = Integer.parseInt(day.trim());
                            if (todayDayOfWeek == targetDay) {
                                weekMatch = true;
                                break;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            } else if (part.startsWith("month:")) {
                hasMonthRule = true;
                String daysStr = part.substring(6);
                if (!daysStr.isEmpty()) {
                    String[] days = daysStr.split(",");
                    int todayDayOfMonth = today.getDayOfMonth();
                    for (String day : days) {
                        try {
                            int targetDay = Integer.parseInt(day.trim());
                            if (todayDayOfMonth == targetDay) {
                                monthMatch = true;
                                break;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }
        
        if (hasWeekRule && hasMonthRule) {
            return weekMatch || monthMatch;
        } else if (hasWeekRule) {
            return weekMatch;
        } else if (hasMonthRule) {
            return monthMatch;
        }
        
        return true;
    }
}
