package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.service.system.UserBehaviorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.TsukasaChan.ShopVault.entity.product.Favorite;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.service.product.FavoriteService;
import com.TsukasaChan.ShopVault.mapper.product.FavoriteMapper;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final UserBehaviorService userBehaviorService;
    private final ProductService productService;

    @Override
    public boolean toggleFavorite(Long userId, Long productId) {
        Favorite exist = getOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));

        if (exist != null) {
            this.removeById(exist.getId());
            return false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            this.save(favorite);

            userBehaviorService.recordBehavior(userId, productId, 2);
            return true;
        }
    }

    @Override
    public List<Favorite> getMyFavorites(Long userId) {
        List<Favorite> favorites = this.list(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));

        if (!favorites.isEmpty()) {
            List<Long> productIds = favorites.stream().map(Favorite::getProductId).distinct().toList();
            Map<Long, Product> productMap = productService.listByIds(productIds)
                    .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

            for (Favorite fav : favorites) {
                Product p = productMap.get(fav.getProductId());
                if (p != null) {
                    fav.setProductName(p.getName());
                    fav.setProductImage(p.getMainImage());
                    fav.setPrice(p.getPrice());
                }
            }
        }

        return favorites;
    }
}