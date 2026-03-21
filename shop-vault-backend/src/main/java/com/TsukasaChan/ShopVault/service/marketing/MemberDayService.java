package com.TsukasaChan.ShopVault.service.marketing;

import com.TsukasaChan.ShopVault.dto.MemberDayDto;
import com.TsukasaChan.ShopVault.entity.marketing.Activity;

import java.math.BigDecimal;
import java.util.List;

public interface MemberDayService {

    Activity getCurrentMemberDay();

    List<Activity> getUpcomingMemberDays();

    MemberDayDto getMemberDayInfo(Long activityId);

    boolean isMemberDayToday();

    BigDecimal applyMemberDayDiscount(BigDecimal originalAmount);

    BigDecimal getMemberDayPointsMultiplier();
}
