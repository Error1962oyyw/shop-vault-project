package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.entity.product.SpecValue;
import com.TsukasaChan.ShopVault.mapper.product.SpecValueMapper;
import com.TsukasaChan.ShopVault.service.product.SpecValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecValueServiceImpl extends ServiceImpl<SpecValueMapper, SpecValue> implements SpecValueService {

    @Override
    public List<SpecValue> listBySpecId(Long specId) {
        return list(new LambdaQueryWrapper<SpecValue>()
                .eq(SpecValue::getSpecId, specId)
                .orderByAsc(SpecValue::getSortOrder));
    }

    @Override
    public void deleteBySpecId(Long specId) {
        remove(new LambdaQueryWrapper<SpecValue>().eq(SpecValue::getSpecId, specId));
    }
}
