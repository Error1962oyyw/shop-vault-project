package com.TsukasaChan.ShopVault.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.TsukasaChan.ShopVault.entity.order.CartItem;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.service.order.CartItemService;
import com.TsukasaChan.ShopVault.mapper.order.CartItemMapper;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartItemService {

    private final ProductService productService;

    @Override
    public void addOrUpdateCart(CartItem cartItem, Long userId) {

        CartItem existItem = this.getOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, cartItem.getProductId()));

        if (existItem != null) {
            existItem.setQuantity(existItem.getQuantity() + cartItem.getQuantity());
            updateById(existItem);
        } else {
            cartItem.setUserId(userId);
            cartItem.setSelected(1);
            this.save(cartItem);
        }
    }

    @Override
    public List<CartItem> listMyCart(Long userId) {
        List<CartItem> items = this.list(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreateTime));

        if (!items.isEmpty()) {
            List<Long> productIds = items.stream().map(CartItem::getProductId).distinct().toList();
            Map<Long, Product> productMap = productService.listByIds(productIds)
                    .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

            for (CartItem item : items) {
                Product p = productMap.get(item.getProductId());
                if (p != null) {
                    item.setProductName(p.getName());
                    item.setProductImage(p.getMainImage());
                    item.setPrice(p.getPrice());
                    item.setStock(p.getStock());
                    item.setFreight(p.getFreight());
                }
            }
        }

        return items;
    }

    @Override
    public void updateQuantity(Long cartItemId, Integer quantity, Long userId) {
        CartItem cartItem = this.getById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作或该商品不在购物车中");
        }
        cartItem.setQuantity(quantity);
        this.updateById(cartItem);
    }

    @Override
    public void deleteCartItems(List<Long> cartItemIds, Long userId) {
        this.remove(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getId, cartItemIds));
    }
}