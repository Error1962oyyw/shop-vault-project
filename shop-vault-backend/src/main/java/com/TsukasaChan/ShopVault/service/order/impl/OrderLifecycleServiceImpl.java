package com.TsukasaChan.ShopVault.service.order.impl;

import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRule;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.mapper.order.OrderMapper;
import com.TsukasaChan.ShopVault.service.marketing.MemberDayService;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
import com.TsukasaChan.ShopVault.service.marketing.PointsRuleService;
import com.TsukasaChan.ShopVault.service.marketing.UserVipInfoService;
import com.TsukasaChan.ShopVault.service.order.OrderLifecycleService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.TsukasaChan.ShopVault.websocket.WebSocketService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderLifecycleServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderLifecycleService {

    private final UserService userService;
    private final WebSocketService webSocketService;
    private final PointsRuleService pointsRuleService;
    private final PointsRecordService pointsRecordService;
    private final MemberDayService memberDayService;
    private final UserVipInfoService userVipInfoService;

    public OrderLifecycleServiceImpl(
            UserService userService,
            WebSocketService webSocketService,
            PointsRuleService pointsRuleService,
            PointsRecordService pointsRecordService,
            MemberDayService memberDayService,
            UserVipInfoService userVipInfoService) {
        this.userService = userService;
        this.webSocketService = webSocketService;
        this.pointsRuleService = pointsRuleService;
        this.pointsRecordService = pointsRecordService;
        this.memberDayService = memberDayService;
        this.userVipInfoService = userVipInfoService;
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

        if (rewardPoints > 0) {
            pointsRecordService.addPoints(userId, rewardPoints, PointsRecord.TYPE_PURCHASE,
                    "订单购买奖励", order.getId());
        }

        userService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<User>()
                        .setSql("credit_score = LEAST(100, credit_score + 2)")
                        .eq(User::getId, userId));

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
