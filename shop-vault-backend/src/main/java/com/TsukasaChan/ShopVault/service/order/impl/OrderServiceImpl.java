package com.TsukasaChan.ShopVault.service.order.impl;

import cn.hutool.core.util.IdUtil;
import com.TsukasaChan.ShopVault.entity.marketing.Activity;
import com.TsukasaChan.ShopVault.entity.marketing.UserCoupon;
import com.TsukasaChan.ShopVault.entity.order.CartItem;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.order.OrderItem;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.manager.RecommendationEngine;
import com.TsukasaChan.ShopVault.mapper.order.OrderMapper;
import com.TsukasaChan.ShopVault.service.marketing.ActivityService;
import com.TsukasaChan.ShopVault.service.marketing.MemberDayService;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
import com.TsukasaChan.ShopVault.service.marketing.PointsRuleService;
import com.TsukasaChan.ShopVault.service.marketing.UserCouponService;
import com.TsukasaChan.ShopVault.service.marketing.UserVipInfoService;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRule;
import com.TsukasaChan.ShopVault.service.order.CartItemService;
import com.TsukasaChan.ShopVault.service.order.OrderItemService;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import com.TsukasaChan.ShopVault.service.product.StockService;
import com.TsukasaChan.ShopVault.service.system.UserBehaviorService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.TsukasaChan.ShopVault.websocket.WebSocketService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final UserService userService;
    private final CartItemService cartItemService;
    private final ActivityService activityService;
    private final MemberDayService memberDayService;
    private final UserCouponService userCouponService;
    private final UserVipInfoService userVipInfoService;
    private final UserBehaviorService userBehaviorService;
    private final StockService stockService;
    private final RecommendationEngine recommendationEngine;
    private final WebSocketService webSocketService;
    private final PointsRuleService pointsRuleService;
    private final PointsRecordService pointsRecordService;

    public OrderServiceImpl(
            ProductService productService,
            OrderItemService orderItemService,
            UserService userService,
            CartItemService cartItemService,
            @Lazy ActivityService activityService,
            MemberDayService memberDayService,
            UserCouponService userCouponService,
            UserVipInfoService userVipInfoService,
            UserBehaviorService userBehaviorService,
            StockService stockService,
            @Lazy RecommendationEngine recommendationEngine,
            WebSocketService webSocketService,
            PointsRuleService pointsRuleService,
            PointsRecordService pointsRecordService) {
        this.productService = productService;
        this.orderItemService = orderItemService;
        this.userService = userService;
        this.cartItemService = cartItemService;
        this.activityService = activityService;
        this.memberDayService = memberDayService;
        this.userCouponService = userCouponService;
        this.userVipInfoService = userVipInfoService;
        this.userBehaviorService = userBehaviorService;
        this.stockService = stockService;
        this.recommendationEngine = recommendationEngine;
        this.webSocketService = webSocketService;
        this.pointsRuleService = pointsRuleService;
        this.pointsRecordService = pointsRecordService;
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

        List<CartItem> cartItems = cartItemService.listByIds(cartItemIds);
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
            for (CartItem cartItem : cartItems) {
                Product product = productService.getById(cartItem.getProductId());

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

    @Override
    public void payOrder(String orderNo, Long userId) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));

        if (order == null) {
            throw new RuntimeException("订单不存在或您无权操作该订单！");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态异常，当前无法进行支付！");
        }

        order.setStatus(1);
        this.updateById(order);

        if (webSocketService.isUserOnline(userId)) {
            webSocketService.notifyUser(userId, "PAYMENT", "支付成功", 
                    "您的订单 " + orderNo + " 已支付成功，商家将尽快发货", orderNo);
        }
    }

    @Override
    public void shipOrder(String orderNo) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order != null && order.getStatus() == 1) {
            order.setStatus(2);
            order.setDeliveryTime(LocalDateTime.now());
            order.setAutoReceiveTime(LocalDateTime.now().plusDays(10));
            this.updateById(order);

            if (webSocketService.isUserOnline(order.getUserId())) {
                webSocketService.notifyUser(order.getUserId(), "SHIPPING", "商品已发货", 
                        "您的订单 " + orderNo + " 已发货，请注意查收", orderNo);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(String orderNo, Long userId) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId));
        if (order == null || order.getStatus() != 2) throw new RuntimeException("订单状态不支持确认收货");

        order.setStatus(3);
        order.setReceiveTime(LocalDateTime.now());
        this.updateById(order);

        int rewardPoints = calculatePurchaseRewardPoints(order, userId);

        User user = userService.getById(userId);
        if (rewardPoints > 0) {
            user.setPoints(user.getPoints() + rewardPoints);
        }
        user.setCreditScore(Math.min(100, user.getCreditScore() + 2));

        userService.updateById(user);

        if (webSocketService.isUserOnline(userId)) {
            webSocketService.notifyUser(userId, "RECEIVE", "确认收货成功", 
                    "您的订单 " + orderNo + " 已确认收货，获得 " + rewardPoints + " 积分", rewardPoints);
        }
    }
    
    private int calculatePurchaseRewardPoints(Order order, Long userId) {
        PointsRule purchaseRule = pointsRuleService.getOne(new LambdaQueryWrapper<PointsRule>()
                .eq(PointsRule::getRuleType, PointsRule.TYPE_PURCHASE)
                .eq(PointsRule::getIsActive, true)
                .last("LIMIT 1"));
        
        BigDecimal baseRatio = BigDecimal.valueOf(100);
        if (purchaseRule != null && purchaseRule.getPointsRatio() != null) {
            baseRatio = purchaseRule.getPointsRatio();
        }
        
        BigDecimal memberDayMultiplier = memberDayService.getMemberDayPointsMultiplier();
        BigDecimal vipMultiplier = userVipInfoService.getPointsMultiplier(userId);
        BigDecimal totalMultiplier = memberDayMultiplier.multiply(vipMultiplier);
        
        int rewardPoints = order.getPayAmount().multiply(baseRatio).multiply(totalMultiplier).intValue();
        
        return Math.max(rewardPoints, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void extendReceiveTime(String orderNo, Long userId) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId));

        if (order == null || order.getStatus() != 2) throw new RuntimeException("订单状态不支持延长收货");
        if (order.getIsExtended() == 1) throw new RuntimeException("每笔订单只能延长一次收货时间");

        User user = userService.getById(userId);
        if (user.getCreditScore() < 60) throw new RuntimeException("您的信誉分过低，无法申请延长收货");

        order.setAutoReceiveTime(order.getAutoReceiveTime().plusDays(5));
        order.setIsExtended(1);
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long userId) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId));
        if (order == null || order.getStatus() != 0) throw new RuntimeException("只能取消待付款的订单");

        order.setStatus(4);
        order.setCloseTime(LocalDateTime.now());
        this.updateById(order);

        if (webSocketService.isUserOnline(userId)) {
            webSocketService.notifyUser(userId, "CANCEL", "订单已取消", 
                    "您的订单 " + orderNo + " 已取消", orderNo);
        }
    }
}
