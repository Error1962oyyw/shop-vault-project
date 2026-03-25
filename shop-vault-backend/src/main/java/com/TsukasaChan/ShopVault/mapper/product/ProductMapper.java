package com.TsukasaChan.ShopVault.mapper.product;

import com.TsukasaChan.ShopVault.entity.product.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT category_id, COUNT(*) as count FROM product WHERE status = 1 GROUP BY category_id")
    List<Map<String, Object>> countProductsByCategory();
}




