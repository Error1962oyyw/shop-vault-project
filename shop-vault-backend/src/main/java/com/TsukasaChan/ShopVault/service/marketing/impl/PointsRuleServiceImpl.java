package com.TsukasaChan.ShopVault.service.marketing.impl;

import com.TsukasaChan.ShopVault.entity.marketing.PointsRule;
import com.TsukasaChan.ShopVault.mapper.marketing.PointsRuleMapper;
import com.TsukasaChan.ShopVault.service.marketing.PointsRuleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsRuleServiceImpl extends ServiceImpl<PointsRuleMapper, PointsRule> implements PointsRuleService {

    @Override
    public List<PointsRule> getAllActiveRules() {
        return this.list(new LambdaQueryWrapper<PointsRule>()
                .eq(PointsRule::getIsActive, true)
                .orderByAsc(PointsRule::getSortOrder));
    }
}
