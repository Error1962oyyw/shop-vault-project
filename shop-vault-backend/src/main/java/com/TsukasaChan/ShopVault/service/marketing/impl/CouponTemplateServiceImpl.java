package com.TsukasaChan.ShopVault.service.marketing.impl;

import com.TsukasaChan.ShopVault.entity.marketing.CouponTemplate;
import com.TsukasaChan.ShopVault.entity.marketing.UserCoupon;
import com.TsukasaChan.ShopVault.mapper.marketing.CouponTemplateMapper;
import com.TsukasaChan.ShopVault.service.marketing.CouponTemplateService;
import com.TsukasaChan.ShopVault.service.marketing.UserCouponService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplate> implements CouponTemplateService {

    private final UserCouponService userCouponService;

    public CouponTemplateServiceImpl(@Lazy UserCouponService userCouponService) {
        this.userCouponService = userCouponService;
    }

    @Override
    public List<CouponTemplate> listAvailable() {
        List<CouponTemplate> templates = list(new LambdaQueryWrapper<CouponTemplate>()
                .eq(CouponTemplate::getStatus, 1)
                .and(wrapper -> wrapper
                        .isNull(CouponTemplate::getValidEndTime)
                        .or()
                        .gt(CouponTemplate::getValidEndTime, LocalDateTime.now()))
                .orderByDesc(CouponTemplate::getCreateTime));
        
        templates.forEach(this::calculateRemainCount);
        return templates;
    }

    @Override
    public boolean canReceive(Long templateId, Long userId) {
        CouponTemplate template = getById(templateId);
        if (template == null || template.getStatus() != 1) {
            return false;
        }
        
        if (template.getValidType() == 1 && template.getValidEndTime() != null 
                && template.getValidEndTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        if (template.getTotalCount() != null && template.getUsedCount() != null
                && template.getTotalCount() - template.getUsedCount() <= 0) {
            return false;
        }
        
        if (template.getPerLimit() != null && template.getPerLimit() > 0) {
            long userReceived = userCouponService.count(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, userId)
                    .eq(UserCoupon::getCouponTemplateId, templateId));
            return userReceived < template.getPerLimit();
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveCoupon(Long templateId, Long userId) {
        if (!canReceive(templateId, userId)) {
            return false;
        }
        
        CouponTemplate template = getById(templateId);
        
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponTemplateId(templateId);
        userCoupon.setStatus(0);
        userCoupon.setGetTime(LocalDateTime.now());
        
        if (template.getValidType() == 1) {
            userCoupon.setExpireTime(template.getValidEndTime());
        } else if (template.getValidType() == 2 && template.getValidDays() != null) {
            userCoupon.setExpireTime(LocalDateTime.now().plusDays(template.getValidDays()));
        }
        
        userCouponService.save(userCoupon);
        
        template.setUsedCount(template.getUsedCount() == null ? 1 : template.getUsedCount() + 1);
        updateById(template);
        
        log.info("用户{}领取优惠券{}", userId, templateId);
        return true;
    }

    @Override
    public CouponTemplate getAvailableById(Long id) {
        CouponTemplate template = getById(id);
        if (template != null && template.getStatus() == 1) {
            calculateRemainCount(template);
            return template;
        }
        return null;
    }

    private void calculateRemainCount(CouponTemplate template) {
        if (template.getTotalCount() != null && template.getUsedCount() != null) {
            template.setRemainCount(template.getTotalCount() - template.getUsedCount());
        } else if (template.getTotalCount() != null) {
            template.setRemainCount(template.getTotalCount());
        } else {
            template.setRemainCount(Integer.MAX_VALUE);
        }
    }
}
