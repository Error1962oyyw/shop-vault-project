package com.TsukasaChan.ShopVault.service.marketing.impl;

import cn.hutool.core.util.IdUtil;
import com.TsukasaChan.ShopVault.common.PageResult;
import com.TsukasaChan.ShopVault.common.RedisDistributedLock;
import com.TsukasaChan.ShopVault.common.ServiceUtils;
import com.TsukasaChan.ShopVault.entity.marketing.*;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.order.OrderItem;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.mapper.marketing.PointsProductMapper;
import com.TsukasaChan.ShopVault.service.marketing.*;
import com.TsukasaChan.ShopVault.service.order.OrderItemService;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PointsProductServiceImpl extends ServiceImpl<PointsProductMapper, PointsProduct> implements PointsProductService {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final PointsRecordService pointsRecordService;
    private final VipMembershipService vipMembershipService;
    private final UserCouponService userCouponService;
    private final CouponTemplateService couponTemplateService;
    private final StringRedisTemplate redisTemplate;
    private final RedisDistributedLock redisDistributedLock;

    @Override
    public List<PointsProduct> getAvailableProducts() {
        List<PointsProduct> products = ServiceUtils.queryList(this, wrapper -> wrapper
                .eq(PointsProduct::getStatus, PointsProduct.STATUS_ENABLED)
                .gt(PointsProduct::getStock, 0)
                .orderByAsc(PointsProduct::getSortOrder));

        for (PointsProduct product : products) {
            product.setRemainStock(product.getStock());
        }
        return products;
    }

    @Override
    public PointsProduct getProductDetail(Long id) {
        PointsProduct product = this.getById(id);
        if (product != null) {
            product.setRemainStock(product.getStock());
        }
        return product;
    }

    @Override
    public String exchangeProduct(Long userId, Long productId) {
        String lockKey = "points_product_exchange:" + productId;
        String lockValue = redisDistributedLock.tryLockWithWait(lockKey, 5, 10, TimeUnit.SECONDS);
        
        if (lockValue == null) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
        
        try {
            return doExchangeProduct(userId, productId);
        } finally {
            redisDistributedLock.releaseLock(lockKey, lockValue);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String doExchangeProduct(Long userId, Long productId) {
        PointsProduct pointsProduct = this.getById(productId);
        if (pointsProduct == null || pointsProduct.getStatus() != PointsProduct.STATUS_ENABLED) {
            throw new RuntimeException("商品不存在或已下架");
        }

        checkDailyLimit(userId, productId, pointsProduct);

        User user = userService.getById(userId);
        if (user.getPoints() < pointsProduct.getPointsCost()) {
            throw new RuntimeException("积分不足，兑换需要 " + pointsProduct.getPointsCost() + " 积分");
        }

        if (pointsProduct.getStock() <= 0) {
            throw new RuntimeException("商品库存不足");
        }

        user.setPoints(user.getPoints() - pointsProduct.getPointsCost());
        userService.updateById(user);

        pointsProduct.setStock(pointsProduct.getStock() - 1);
        this.updateById(pointsProduct);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(-pointsProduct.getPointsCost());
        record.setType(PointsRecord.TYPE_EXCHANGE);
        record.setDescription("积分兑换: " + pointsProduct.getName());
        pointsRecordService.save(record);

        recordDailyExchange(userId, productId);

        return processExchange(userId, pointsProduct);
    }

    private void checkDailyLimit(Long userId, Long productId, PointsProduct product) {
        if (product.getDailyLimit() != null && product.getDailyLimit() > 0) {
            String key = "points:exchange:" + userId + ":" + productId + ":" + LocalDate.now();
            String count = redisTemplate.opsForValue().get(key);
            if (count != null && Integer.parseInt(count) >= product.getDailyLimit()) {
                throw new RuntimeException("今日兑换次数已达上限");
            }
        }
    }

    private void recordDailyExchange(Long userId, Long productId) {
        String key = "points:exchange:" + userId + ":" + productId + ":" + LocalDate.now();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    private String processExchange(Long userId, PointsProduct pointsProduct) {
        switch (pointsProduct.getType()) {
            case PointsProduct.TYPE_SMALL_PRODUCT:
                return createPhysicalOrder(userId, pointsProduct);
            case PointsProduct.TYPE_COUPON:
                return grantCoupon(userId, pointsProduct);
            case PointsProduct.TYPE_VIP_MONTHLY:
                vipMembershipService.exchangeVip(userId, VipMembership.TYPE_MONTHLY);
                return "VIP_MONTHLY_ACTIVATED";
            case PointsProduct.TYPE_VIP_YEARLY:
                vipMembershipService.exchangeVip(userId, VipMembership.TYPE_YEARLY);
                return "VIP_YEARLY_ACTIVATED";
            default:
                throw new RuntimeException("未知的商品类型");
        }
    }

    private String createPhysicalOrder(Long userId, PointsProduct pointsProduct) {
        Product product = productService.getById(pointsProduct.getRelatedId());
        if (product == null) {
            throw new RuntimeException("关联商品不存在");
        }

        String orderNo = IdUtil.getSnowflakeNextIdStr();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(product.getPrice());
        order.setPayAmount(BigDecimal.ZERO);
        order.setStatus(1);
        order.setOrderType(Order.ORDER_TYPE_POINTS_EXCHANGE);
        order.setIsPointsExchange(1);
        order.setAfterSalesDisabled(1);
        orderService.save(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setOrderNo(orderNo);
        item.setProductId(product.getId());
        item.setProductName(product.getName() + " [积分兑换]");
        item.setProductImg(product.getMainImage());
        item.setProductPrice(product.getPrice());
        item.setQuantity(1);
        item.setIsPointsExchange(1);
        orderItemService.save(item);

        return orderNo;
    }

    private String grantCoupon(Long userId, PointsProduct pointsProduct) {
        CouponTemplate template = couponTemplateService.getById(pointsProduct.getRelatedId());
        if (template == null) {
            throw new RuntimeException("优惠券模板不存在");
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponTemplateId(template.getId());
        userCoupon.setStatus(0);
        userCoupon.setGetTime(LocalDateTime.now());
        
        if (template.getValidType() == 1) {
            userCoupon.setExpireTime(template.getValidEndTime());
        } else {
            userCoupon.setExpireTime(LocalDateTime.now().plusDays(template.getValidDays()));
        }
        
        userCouponService.save(userCoupon);
        return "COUPON_GRANTED_" + userCoupon.getId();
    }

    @Override
    public PageResult<PointsProduct> getAdminPage(Integer pageNum, Integer pageSize, Integer type) {
        return ServiceUtils.queryPage(this, pageNum, pageSize, wrapper -> {
            if (type != null) wrapper.eq(PointsProduct::getType, type);
            wrapper.orderByAsc(PointsProduct::getSortOrder);
        });
    }
}
