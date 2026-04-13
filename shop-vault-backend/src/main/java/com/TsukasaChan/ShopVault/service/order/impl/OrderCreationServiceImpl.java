package com.TsukasaChan.ShopVault.service.order.impl;

import cn.hutool.core.util.IdUtil;
import com.TsukasaChan.ShopVault.entity.marketing.Activity;
import com.TsukasaChan.ShopVault.entity.marketing.UserCoupon;
import com.TsukasaChan.ShopVault.entity.order.CartItem;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.order.OrderItem;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.manager.RecommendationEngine;
import com.TsukasaChan.ShopVault.mapper.order.OrderMapper;
import com.TsukasaChan.ShopVault.service.marketing.ActivityService;
import com.TsukasaChan.ShopVault.service.marketing.MemberDayService;
import com.TsukasaChan.ShopVault.service.marketing.UserCouponService;
import com.TsukasaChan.ShopVault.service.marketing.UserVipInfoService;
import com.TsukasaChan.ShopVault.service.order.CartItemService;
import com.TsukasaChan.ShopVault.service.order.OrderCreationService;
import com.TsukasaChan.ShopVault.service.order.OrderItemService;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import com.TsukasaChan.ShopVault.service.product.StockService;
import com.TsukasaChan.ShopVault.service.system.UserBehaviorService;
import com.TsukasaChan.ShopVault.websocket.WebSocketService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderCreationServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderCreationService {

    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final CartItemService cartItemService;
    private final ActivityService activityService;
    private final MemberDayService memberDayService;
    private final UserCouponService userCouponService;
    private final UserVipInfoService userVipInfoService;
    private final UserBehaviorService userBehaviorService;
    private final StockService stockService;
    private final RecommendationEngine recommendationEngine;
    private final WebSocketService webSocketService;
    private final StringRedisTemplate redisTemplate;

    private static final String IDEMPOTENT_PREFIX = "order:idempotent:";
    private static final int IDEMPOTENT_SECONDS = 5;

    public OrderCreationServiceImpl(
            ProductService productService,
            OrderItemService orderItemService,
            CartItemService cartItemService,
            ActivityService activityService,
            MemberDayService memberDayService,
            UserCouponService userCouponService,
            UserVipInfoService userVipInfoService,
            UserBehaviorService userBehaviorService,
            StockService stockService,
            RecommendationEngine recommendationEngine,
            WebSocketService webSocketService,
            StringRedisTemplate redisTemplate) {
        this.productService = productService;
        this.orderItemService = orderItemService;
        this.cartItemService = cartItemService;
        this.activityService = activityService;
        this.memberDayService = memberDayService;
        this.userCouponService = userCouponService;
        this.userVipInfoService = userVipInfoService;
        this.userBehaviorService = userBehaviorService;
        this.stockService = stockService;
        this.recommendationEngine = recommendationEngine;
        this.webSocketService = webSocketService;
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("null")
    private void checkIdempotent(String key) {
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(key, "1", IDEMPOTENT_SECONDS, TimeUnit.SECONDS);
        if (absent == null || !absent) {
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }
    }

    private void checkUnpaidLimit(Long userId) {
        long unpaidCount = this.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, 0));
        if (unpaidCount >= 3) {
            throw new RuntimeException("您有太多未付款的订单，请先支付或取消后再下单！");
        }
    }

    private Order buildOrder(Long userId, BigDecimal totalAmount, String orderNo, Long userCouponId) {
        BigDecimal payAmount = memberDayService.applyMemberDayDiscount(totalAmount);

        BigDecimal vipDiscountRate = userVipInfoService.getDiscountRate(userId);
        if (vipDiscountRate.compareTo(BigDecimal.ONE) < 0) {
            payAmount = payAmount.multiply(vipDiscountRate).setScale(2, RoundingMode.HALF_UP);
        }

        if (userCouponId != null) {
            UserCoupon userCoupon = userCouponService.getById(userCouponId);
            if (userCoupon != null
                    && userCoupon.getUserId().equals(userId)
                    && userCoupon.getStatus() == 0
                    && userCoupon.getExpireTime().isAfter(LocalDateTime.now())) {

                Activity couponRule = activityService.getById(userCoupon.getActivityId());
                if (couponRule != null && couponRule.getDiscountRate() != null) {
                    payAmount = payAmount.multiply(couponRule.getDiscountRate()).setScale(2, RoundingMode.HALF_UP);

                    userCoupon.setStatus(1);
                    userCoupon.setUseTime(LocalDateTime.now());
                    userCouponService.updateById(userCoupon);
                }
            } else {
                throw new RuntimeException("该优惠券无效或已过期");
            }
        }

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setStatus(0);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String buyNow(Long userId, Long productId, Integer quantity, Long userCouponId) {
        checkUnpaidLimit(userId);
        checkIdempotent(IDEMPOTENT_PREFIX + userId + ":buy:" + productId);
        String orderNo = IdUtil.getSnowflakeNextIdStr();

        Product product = productService.getById(productId);
        if (product == null || product.getStatus() == 0) throw new RuntimeException("商品已下架");

        if (stockService.isDeductStockFailed(productId, quantity)) {
            throw new RuntimeException("库存不足，请稍后重试");
        }

        try {
            BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));

            OrderItem item = new OrderItem();
            item.setOrderNo(orderNo);
            item.setProductId(productId);
            item.setProductName(product.getName());
            item.setProductImg(product.getMainImage());
            item.setProductPrice(product.getPrice());
            item.setQuantity(quantity);

            Order order = buildOrder(userId, totalAmount, orderNo, userCouponId);
            this.save(order);

            item.setOrderId(order.getId());
            orderItemService.save(item);

            userBehaviorService.recordBehavior(userId, productId, 4);
            recommendationEngine.recordBehaviorAndUpdateRecommendation(userId, productId, 4);

            if (webSocketService.isUserOnline(userId)) {
                webSocketService.notifyUser(userId, "ORDER", "订单创建成功",
                        "您的订单 " + orderNo + " 已创建成功，请尽快支付", null);
            }

            return orderNo;
        } catch (Exception e) {
            stockService.restoreStock(productId, quantity);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String cartCheckout(Long userId, List<Long> cartItemIds, Long userCouponId) {
        checkUnpaidLimit(userId);
        if (cartItemIds == null || cartItemIds.isEmpty()) throw new RuntimeException("未选择商品");
        checkIdempotent(IDEMPOTENT_PREFIX + userId + ":cart:" + cartItemIds.stream().sorted().toList());

        List<CartItem> cartItems = cartItemService.listByIds(cartItemIds);
        cartItems = cartItems.stream()
                .filter(item -> item.getUserId().equals(userId))
                .toList();
        if (cartItems.isEmpty()) throw new RuntimeException("未找到有效的购物车商品");
        String orderNo = IdUtil.getSnowflakeNextIdStr();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Map<Long, Integer> stockDeductions = new HashMap<>();

        for (CartItem cartItem : cartItems) {
            Product product = productService.getById(cartItem.getProductId());
            if (product == null) {
                throw new RuntimeException("商品 [ID: " + cartItem.getProductId() + "] 不存在");
            }
            if (product.getStatus() == 0) {
                throw new RuntimeException("商品 [" + product.getName() + "] 已下架");
            }
            stockDeductions.put(product.getId(), cartItem.getQuantity());
        }

        if (!stockService.deductStockBatch(stockDeductions)) {
            throw new RuntimeException("部分商品库存不足，请刷新后重试");
        }

        try {
            List<Long> productIds = cartItems.stream()
                    .map(CartItem::getProductId)
                    .distinct()
                    .toList();
            Map<Long, Product> productMap = productService.listByIds(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            for (CartItem cartItem : cartItems) {
                Product product = productMap.get(cartItem.getProductId());
                if (product == null) {
                    throw new RuntimeException("商品 [ID: " + cartItem.getProductId() + "] 不存在");
                }

                BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);

                OrderItem item = new OrderItem();
                item.setOrderNo(orderNo);
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setProductPrice(product.getPrice());
                item.setQuantity(cartItem.getQuantity());
                orderItems.add(item);

                userBehaviorService.recordBehavior(userId, product.getId(), 4);
                recommendationEngine.recordBehaviorAndUpdateRecommendation(userId, product.getId(), 4);
            }

            Order order = buildOrder(userId, totalAmount, orderNo, userCouponId);
            this.save(order);

            for (OrderItem item : orderItems) {
                item.setOrderId(order.getId());
            }
            orderItemService.saveBatch(orderItems);

            cartItemService.removeByIds(cartItemIds);

            if (webSocketService.isUserOnline(userId)) {
                webSocketService.notifyUser(userId, "ORDER", "订单创建成功",
                        "您的订单 " + orderNo + " 已创建成功，请尽快支付", null);
            }

            return orderNo;
        } catch (Exception e) {
            for (Map.Entry<Long, Integer> entry : stockDeductions.entrySet()) {
                stockService.restoreStock(entry.getKey(), entry.getValue());
            }
            throw e;
        }
    }
}
