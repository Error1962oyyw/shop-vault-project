package com.TsukasaChan.ShopVault.service.marketing.impl;

import cn.hutool.core.util.IdUtil;
import com.TsukasaChan.ShopVault.common.PageResult;
import com.TsukasaChan.ShopVault.common.ServiceUtils;
import com.TsukasaChan.ShopVault.entity.marketing.Activity;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.order.OrderItem;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.mapper.marketing.ActivityMapper;
import com.TsukasaChan.ShopVault.service.marketing.ActivityService;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
import com.TsukasaChan.ShopVault.service.order.OrderItemService;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final PointsRecordService pointsRecordService;

    public ActivityServiceImpl(
            UserService userService,
            ProductService productService,
            @Lazy OrderService orderService,
            OrderItemService orderItemService,
            PointsRecordService pointsRecordService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.pointsRecordService = pointsRecordService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String exchangeProduct(Long userId, Long activityId) {
        // 1. 校验活动合法性
        Activity activity = this.getById(activityId);
        LocalDateTime now = LocalDateTime.now();
        if (activity == null || activity.getStatus() != Activity.STATUS_ENABLED || activity.getType() != Activity.TYPE_POINTS_EXCHANGE) {
            throw new RuntimeException("该积分兑换活动不存在或已下线");
        }
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new RuntimeException("不在活动时间范围内");
        }

        // 2. 校验商品库存与用户积分
        Product product = productService.getById(activity.getProductId());
        if (product == null || product.getStock() < 1) {
            throw new RuntimeException("抱歉，该兑换商品已被抢空");
        }

        User user = userService.getById(userId);
        if (user.getPoints() < activity.getPointCost()) {
            throw new RuntimeException("积分不足，兑换需要 " + activity.getPointCost() + " 积分");
        }

        // 3. 扣除积分与库存
        user.setPoints(user.getPoints() - activity.getPointCost());
        userService.updateById(user);

        product.setStock(product.getStock() - 1);
        productService.updateById(product);

        // 4. 记录积分流水
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setType(PointsRecord.TYPE_EXCHANGE);
        record.setPoints(-activity.getPointCost());
        record.setDescription("参与活动: " + activity.getName() + " 兑换商品");
        pointsRecordService.save(record);

        // 5. 生成一笔0元发货订单，融入现有订单体系
        String orderNo = IdUtil.getSnowflakeNextIdStr();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(product.getPrice());
        order.setPayAmount(BigDecimal.ZERO); // 实付 0 元
        order.setStatus(1); // 直接跳过待付款，进入 [待发货] 状态
        orderService.save(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setOrderNo(orderNo);
        item.setProductId(product.getId());
        item.setProductName(product.getName() + " [积分兑换]");
        item.setProductImg(product.getMainImage());
        item.setProductPrice(product.getPrice());
        item.setQuantity(1);
        orderItemService.save(item);

        return orderNo;
    }

    @Override
    public List<Activity> getAvailableCoupons() {
        return ServiceUtils.queryList(this, wrapper -> wrapper
                .eq(Activity::getType, Activity.TYPE_COUPON)
                .eq(Activity::getStatus, Activity.STATUS_ENABLED)
                .le(Activity::getStartTime, LocalDateTime.now())
                .ge(Activity::getEndTime, LocalDateTime.now()));
    }

    @Override
    public PageResult<Activity> getActivityPage(Integer pageNum, Integer pageSize, Integer type, Integer status) {
        return ServiceUtils.queryPage(this, pageNum, pageSize, wrapper -> {
            if (type != null) wrapper.eq(Activity::getType, type);
            if (status != null) wrapper.eq(Activity::getStatus, status);
            wrapper.orderByDesc(Activity::getCreateTime);
        });
    }

    @Override
    public List<Activity> getMemberDayActivities() {
        return ServiceUtils.queryList(this, wrapper -> wrapper
                .eq(Activity::getType, Activity.TYPE_MEMBER_DAY)
                .eq(Activity::getStatus, Activity.STATUS_ENABLED)
                .orderByDesc(Activity::getStartTime));
    }
}