package com.TsukasaChan.ShopVault.service.marketing;

import com.TsukasaChan.ShopVault.common.PageResult;
import com.TsukasaChan.ShopVault.entity.marketing.PointsProduct;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PointsProductService extends IService<PointsProduct> {

    List<PointsProduct> getAvailableProducts();

    PointsProduct getProductDetail(Long id);

    String exchangeProduct(Long userId, Long productId);

    PageResult<PointsProduct> getAdminPage(Integer pageNum, Integer pageSize, Integer type);
}
