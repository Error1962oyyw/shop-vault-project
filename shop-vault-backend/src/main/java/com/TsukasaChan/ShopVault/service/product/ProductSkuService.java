package com.TsukasaChan.ShopVault.service.product;

import com.TsukasaChan.ShopVault.entity.product.ProductSku;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ProductSkuService extends IService<ProductSku> {

    List<ProductSku> listByProductId(Long productId);

    ProductSku getBySkuCode(String skuCode);

    boolean deductStock(Long skuId, Integer quantity);

    boolean addStock(Long skuId, Integer quantity);

    void deleteByProductId(Long productId);
}
