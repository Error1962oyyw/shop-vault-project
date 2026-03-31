package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.entity.product.ProductSpec;
import com.TsukasaChan.ShopVault.mapper.product.ProductSpecMapper;
import com.TsukasaChan.ShopVault.service.product.ProductSpecService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductSpecServiceImpl extends ServiceImpl<ProductSpecMapper, ProductSpec> implements ProductSpecService {

    @Override
    public List<ProductSpec> listByProductId(Long productId) {
        return list(new LambdaQueryWrapper<ProductSpec>()
                .eq(ProductSpec::getProductId, productId)
                .orderByAsc(ProductSpec::getSortOrder));
    }

    @Override
    public void deleteByProductId(Long productId) {
        remove(new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, productId));
    }

    @Override
    public ProductSpec getByProductIdAndSpecId(Long productId, Long specId) {
        return getOne(new LambdaQueryWrapper<ProductSpec>()
                .eq(ProductSpec::getProductId, productId)
                .eq(ProductSpec::getSpecId, specId));
    }
}
