package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.entity.product.ProductSpecValue;
import com.TsukasaChan.ShopVault.mapper.product.ProductSpecValueMapper;
import com.TsukasaChan.ShopVault.service.product.ProductSpecValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductSpecValueServiceImpl extends ServiceImpl<ProductSpecValueMapper, ProductSpecValue> implements ProductSpecValueService {

    @Override
    public List<ProductSpecValue> listByProductSpecId(Long productSpecId) {
        return list(new LambdaQueryWrapper<ProductSpecValue>()
                .eq(ProductSpecValue::getProductSpecId, productSpecId)
                .orderByAsc(ProductSpecValue::getSortOrder));
    }

    @Override
    public void deleteByProductSpecId(Long productSpecId) {
        remove(new LambdaQueryWrapper<ProductSpecValue>().eq(ProductSpecValue::getProductSpecId, productSpecId));
    }

    @Override
    public boolean existsByValue(Long productSpecId, String value) {
        return count(new LambdaQueryWrapper<ProductSpecValue>()
                .eq(ProductSpecValue::getProductSpecId, productSpecId)
                .eq(ProductSpecValue::getValue, value)) > 0;
    }
}
