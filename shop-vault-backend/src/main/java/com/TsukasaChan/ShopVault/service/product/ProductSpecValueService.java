package com.TsukasaChan.ShopVault.service.product;

import com.TsukasaChan.ShopVault.entity.product.ProductSpecValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ProductSpecValueService extends IService<ProductSpecValue> {

    List<ProductSpecValue> listByProductSpecId(Long productSpecId);

    void deleteByProductSpecId(Long productSpecId);

    boolean existsByValue(Long productSpecId, String value);
}
