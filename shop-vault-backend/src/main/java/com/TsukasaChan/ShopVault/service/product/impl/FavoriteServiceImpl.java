package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.service.system.UserBehaviorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.TsukasaChan.ShopVault.entity.product.Favorite;
import com.TsukasaChan.ShopVault.service.product.FavoriteService;
import com.TsukasaChan.ShopVault.mapper.product.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final UserBehaviorService userBehaviorService;

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
        return this.list(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));
    }
}