package com.TsukasaChan.ShopVault.service.product;

import com.TsukasaChan.ShopVault.entity.product.Spec;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SpecService extends IService<Spec> {

    List<Spec> listWithValues();

    Spec getByIdWithValues(Long id);
}
