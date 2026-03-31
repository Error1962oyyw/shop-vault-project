package com.TsukasaChan.ShopVault.service.product;

import com.TsukasaChan.ShopVault.entity.product.ProductSpec;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ProductSpecService extends IService<ProductSpec> {

    List<ProductSpec> listByProductId(Long productId);

    void deleteByProductId(Long productId);

    ProductSpec getByProductIdAndSpecId(Long productId, Long specId);
}
