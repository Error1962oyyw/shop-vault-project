package com.TsukasaChan.ShopVault.service.order.impl;

import com.TsukasaChan.ShopVault.common.VipConstants;
import com.TsukasaChan.ShopVault.util.OrderNoGenerator;
import com.TsukasaChan.ShopVault.dto.CreateOrderDto;
import com.TsukasaChan.ShopVault.dto.OrderDetailDto;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.order.OrderItem;
import com.TsukasaChan.ShopVault.entity.order.PaymentRecord;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.entity.marketing.PointsProduct;
import com.TsukasaChan.ShopVault.mapper.order.OrderMapper;
import com.TsukasaChan.ShopVault.service.order.*;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.TsukasaChan.ShopVault.service.marketing.PointsProductService;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
import com.TsukasaChan.ShopVault.service.marketing.VipMembershipService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedOrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements UnifiedOrderService {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final PaymentRecordService paymentRecordService;
    private final UserService userService;
    private final PointsProductService pointsProductService;
    private final PointsRecordService pointsRecordService;
    private final VipMembershipService vipMembershipService;

    private static final int ORDER_EXPIRE_HOURS = 24;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, CreateOrderDto dto) {
        Order order = new Order();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setUserId(userId);
        order.setOrderType(dto.getOrderType() != null ? dto.getOrderType() : Order.ORDER_TYPE_NORMAL);
        order.setStatus(Order.STATUS_PENDING_PAYMENT);
        order.setExpireTime(LocalDateTime.now().plusHours(ORDER_EXPIRE_HOURS));
        order.setCreateTime(LocalDateTime.now());
        
        if (dto.getOrderType() != null && (dto.getOrderType() == Order.ORDER_TYPE_VIP || 
                dto.getOrderType() == Order.ORDER_TYPE_SVIP || dto.getOrderType() == Order.ORDER_TYPE_POINTS_EXCHANGE)) {
            order.setDiscountDisabled(1);
            order.setAfterSalesDisabled(1);
        }
        
        order.setRemark(dto.getRemark());
        save(order);
        
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createVipOrder(Long userId, int vipType, String paymentMethod) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Order order = new Order();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setUserId(userId);
        order.setOrderType(vipType == VipConstants.TYPE_VIP_MONTHLY ? Order.ORDER_TYPE_VIP : Order.ORDER_TYPE_SVIP);
        order.setStatus(Order.STATUS_PENDING_PAYMENT);
        order.setExpireTime(LocalDateTime.now().plusHours(ORDER_EXPIRE_HOURS));
        order.setPaymentMethod(paymentMethod);
        order.setDiscountDisabled(1);
        order.setAfterSalesDisabled(1);
        
        BigDecimal price = VipConstants.getPriceByType(vipType);
        order.setTotalAmount(price);
        order.setPayAmount(price);
        order.setCreateTime(LocalDateTime.now());
        
        String productName = vipType == VipConstants.TYPE_VIP_MONTHLY ? "VIP月卡" : "SVIP年卡";
        order.setProductName(productName);
        
        save(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setOrderNo(order.getOrderNo());
        item.setProductId(0L);
        item.setProductName(productName);
        item.setProductPrice(price);
        item.setQuantity(1);
        orderItemService.save(item);

        paymentRecordService.createPaymentRecord(order.getId(), order.getOrderNo(), userId, 
                paymentMethod, price, 0);

        log.info("创建VIP订单成功, orderNo={}, userId={}, vipType={}", order.getOrderNo(), userId, vipType);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createPointsExchangeOrder(Long userId, Long productId, Integer quantity) {
        PointsProduct pointsProduct = pointsProductService.getById(productId);
        if (pointsProduct == null || pointsProduct.getStatus() != 1) {
            throw new RuntimeException("商品不存在或已下架");
        }

        if (pointsProduct.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        int totalPoints = pointsProduct.getPointsCost() * quantity;
        if (user.getPoints() < totalPoints) {
            throw new RuntimeException("积分不足");
        }

        Order order = new Order();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setUserId(userId);
        order.setOrderType(Order.ORDER_TYPE_POINTS_EXCHANGE);
        order.setStatus(Order.STATUS_PENDING_PAYMENT);
        order.setExpireTime(LocalDateTime.now().plusHours(ORDER_EXPIRE_HOURS));
        order.setPaymentMethod(Order.PAYMENT_METHOD_POINTS);
        order.setDiscountDisabled(1);
        order.setAfterSalesDisabled(1);
        order.setIsPointsExchange(1);
        order.setPointsAmount(totalPoints);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setPayAmount(BigDecimal.ZERO);
        order.setCreateTime(LocalDateTime.now());
        order.setProductName(pointsProduct.getName());
        
        save(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setOrderNo(order.getOrderNo());
        item.setProductId(productId);
        item.setProductName(pointsProduct.getName());
        item.setProductImg(pointsProduct.getMainImage());
        item.setProductPrice(BigDecimal.ZERO);
        item.setQuantity(quantity);
        item.setIsPointsExchange(1);
        orderItemService.save(item);

        paymentRecordService.createPaymentRecord(order.getId(), order.getOrderNo(), userId, 
                Order.PAYMENT_METHOD_POINTS, BigDecimal.ZERO, totalPoints);

        log.info("创建积分兑换订单成功, orderNo={}, userId={}, productId={}", order.getOrderNo(), userId, productId);
        return order;
    }

    @Override
    public Order getOrderByNo(String orderNo) {
        return getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
    }

    @Override
    public IPage<OrderDetailDto> getUserOrders(Long userId, Integer status, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        
        IPage<Order> orderPage = page(pageParam, wrapper);
        
        return orderPage.convert(this::convertToDetailDto);
    }

    @Override
    public OrderDetailDto getOrderDetail(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return null;
        }
        return convertToDetailDto(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrderByBalance(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
            throw new RuntimeException("订单状态不正确");
        }

        if (order.getExpireTime() != null && LocalDateTime.now().isAfter(order.getExpireTime())) {
            throw new RuntimeException("订单已过期");
        }

        User user = userService.getById(userId);
        if (user.getBalance().compareTo(order.getPayAmount()) < 0) {
            throw new RuntimeException("余额不足");
        }

        PaymentRecord record = paymentRecordService.getByOrderNo(order.getOrderNo());
        if (record == null) {
            throw new RuntimeException("支付记录不存在");
        }

        boolean updated = userService.updateBalanceWithLock(userId, order.getPayAmount().negate());
        if (!updated) {
            throw new RuntimeException("余额扣除失败");
        }

        order.setStatus(Order.STATUS_PENDING_DELIVERY);
        order.setPaymentMethod(Order.PAYMENT_METHOD_BALANCE);
        order.setPaymentTime(LocalDateTime.now());
        updateById(order);

        paymentRecordService.updatePaymentSuccess(record.getId(), null);

        if (order.getOrderType() == Order.ORDER_TYPE_VIP || order.getOrderType() == Order.ORDER_TYPE_SVIP) {
            processVipOrderAfterPayment(order);
        }

        log.info("余额支付成功, orderNo={}, userId={}", order.getOrderNo(), userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrderByPoints(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
            throw new RuntimeException("订单状态不正确");
        }

        if (order.getOrderType() != Order.ORDER_TYPE_POINTS_EXCHANGE) {
            throw new RuntimeException("非积分兑换订单不能使用积分支付");
        }

        User user = userService.getById(userId);
        if (user.getPoints() < order.getPointsAmount()) {
            throw new RuntimeException("积分不足");
        }

        PaymentRecord record = paymentRecordService.getByOrderNo(order.getOrderNo());
        if (record == null) {
            throw new RuntimeException("支付记录不存在");
        }

        pointsRecordService.addPoints(userId, -order.getPointsAmount(), 
                "POINTS_EXCHANGE", "积分兑换商品", order.getId());

        order.setStatus(Order.STATUS_COMPLETED);
        order.setPaymentMethod(Order.PAYMENT_METHOD_POINTS);
        order.setPaymentTime(LocalDateTime.now());
        updateById(order);

        paymentRecordService.updatePaymentSuccess(record.getId(), null);

        log.info("积分支付成功, orderNo={}, userId={}, points={}", order.getOrderNo(), userId, order.getPointsAmount());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long userId, Long orderId, String reason) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
            throw new RuntimeException("只能取消待支付订单");
        }

        order.setStatus(Order.STATUS_CLOSED);
        order.setCloseTime(LocalDateTime.now());
        order.setCloseReason(reason);
        updateById(order);

        log.info("订单取消成功, orderNo={}, userId={}, reason={}", order.getOrderNo(), userId, reason);
        return true;
    }

    @Override
    public void cancelExpiredOrders() {
        List<Order> expiredOrders = getExpiredOrders();
        for (Order order : expiredOrders) {
            try {
                orderService.cancelOrder(order.getOrderNo(), order.getUserId());
                log.info("订单超时自动取消, orderNo={}", order.getOrderNo());
            } catch (Exception e) {
                log.error("取消超时订单失败, orderNo={}", order.getOrderNo(), e);
            }
        }
    }

    @Override
    public void processOrderPayment(Long orderId, String paymentMethod) {
        Order order = getById(orderId);
        if (order == null) {
            return;
        }

        if (Order.PAYMENT_METHOD_BALANCE.equals(paymentMethod)) {
            payOrderByBalance(order.getUserId(), orderId);
        } else if (Order.PAYMENT_METHOD_POINTS.equals(paymentMethod)) {
            payOrderByPoints(order.getUserId(), orderId);
        }
    }

    @Override
    public List<Order> getExpiredOrders() {
        return list(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, Order.STATUS_PENDING_PAYMENT)
                .lt(Order::getExpireTime, LocalDateTime.now()));
    }

    private void processVipOrderAfterPayment(Order order) {
        int vipType = order.getOrderType() == Order.ORDER_TYPE_VIP ? VipConstants.TYPE_VIP_MONTHLY : VipConstants.TYPE_SVIP_YEARLY;
        vipMembershipService.purchaseVip(order.getUserId(), vipType, "PURCHASE");
        log.info("VIP订单支付后处理完成, userId={}, vipType={}", order.getUserId(), vipType);
    }

    private OrderDetailDto convertToDetailDto(Order order) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setOrderType(order.getOrderType());
        dto.setOrderTypeName(getOrderTypeName(order.getOrderType()));
        dto.setStatus(order.getStatus());
        dto.setStatusName(getStatusName(order.getStatus()));
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPayAmount(order.getPayAmount());
        dto.setPointsAmount(order.getPointsAmount());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentMethodName(getPaymentMethodName(order.getPaymentMethod()));
        dto.setExpireTime(order.getExpireTime());
        dto.setPaymentTime(order.getPaymentTime());
        dto.setCreateTime(order.getCreateTime());
        dto.setCloseTime(order.getCloseTime());
        dto.setCloseReason(order.getCloseReason());
        dto.setTrackingCompany(order.getTrackingCompany());
        dto.setTrackingNo(order.getTrackingNo());
        dto.setProductName(order.getProductName());
        dto.setRemark(order.getRemark());
        return dto;
    }

    private String getOrderTypeName(Integer orderType) {
        if (orderType == null) return "普通商品";
        return switch (orderType) {
            case 1 -> "VIP购买";
            case 2 -> "SVIP购买";
            case 3 -> "积分兑换";
            default -> "普通商品";
        };
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "已完成";
            case 4 -> "已关闭";
            case 5 -> "售后中";
            default -> "未知";
        };
    }

    private String getPaymentMethodName(String paymentMethod) {
        if (paymentMethod == null) return "未选择";
        return switch (paymentMethod) {
            case "BALANCE" -> "余额支付";
            case "POINTS" -> "积分支付";
            case "ALIPAY" -> "支付宝";
            case "WECHAT" -> "微信支付";
            default -> "未知";
        };
    }
}
