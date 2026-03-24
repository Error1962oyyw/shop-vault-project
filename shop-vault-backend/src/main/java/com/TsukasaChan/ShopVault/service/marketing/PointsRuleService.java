package com.TsukasaChan.ShopVault.service.marketing;

import com.TsukasaChan.ShopVault.entity.marketing.PointsRule;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PointsRuleService extends IService<PointsRule> {

    List<PointsRule> getAllActiveRules();
}
