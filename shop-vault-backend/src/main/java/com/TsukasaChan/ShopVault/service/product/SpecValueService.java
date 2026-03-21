package com.TsukasaChan.ShopVault.service.product;

import com.TsukasaChan.ShopVault.entity.product.SpecValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SpecValueService extends IService<SpecValue> {

    List<SpecValue> listBySpecId(Long specId);

    void deleteBySpecId(Long specId);
}
