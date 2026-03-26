package com.TsukasaChan.ShopVault.service.order.impl;

import com.TsukasaChan.ShopVault.dto.AfterSalesApplyDto;
import com.TsukasaChan.ShopVault.dto.AfterSalesHandleDto;
import com.TsukasaChan.ShopVault.dto.ReturnLogisticsDto;
import com.TsukasaChan.ShopVault.entity.order.AfterSales;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.mapper.order.AfterSalesMapper;
import com.TsukasaChan.ShopVault.service.order.AfterSalesService;
import com.TsukasaChan.ShopVault.service.order.OrderItemService;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AfterSalesServiceImpl extends ServiceImpl<AfterSalesMapper, AfterSales> implements AfterSalesService {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderItemService orderItemService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyAfterSales(AfterSalesApplyDto dto, Long userId) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, dto.getOrderNo())
                .eq(Order::getUserId, userId));

        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getAfterSalesDisabled() != null && order.getAfterSalesDisabled() == 1) {
            throw new RuntimeException("积分兑换商品不支持售后申请");
        }

        if (order.getIsPointsExchange() != null && order.getIsPointsExchange() == 1) {
            throw new RuntimeException("积分兑换订单不支持售后申请");
        }

        if (order.getStatus() != 1 && order.getStatus() != 2 && order.getStatus() != 3) {
            throw new RuntimeException("当前订单状态不支持申请售后");
        }

        long count = this.count(new LambdaQueryWrapper<AfterSales>()
                .eq(AfterSales::getOrderNo, order.getOrderNo())
                .ne(AfterSales::getStatus, AfterSales.STATUS_CANCELLED)
                .ne(AfterSales::getStatus, AfterSales.STATUS_REJECTED));
        if (count > 0) {
            throw new RuntimeException("该订单已存在进行中的售后申请");
        }

        User user = userService.getById(userId);
        if (order.getStatus() == 1 && user.getCreditScore() >= 90 && dto.getType() == AfterSales.TYPE_REFUND_ONLY) {
            AfterSales afterSales = createAfterSales(dto, order, userId);
            afterSales.setStatus(AfterSales.STATUS_COMPLETED);
            afterSales.setMerchantReply("信誉极好，系统自动秒退款");
            afterSales.setMerchantHandleTime(LocalDateTime.now());
            this.save(afterSales);

            order.setStatus(4);
            order.setCloseTime(LocalDateTime.now());
            orderService.updateById(order);

            orderItemService.restoreInventoryByOrderId(order.getId());
            return;
        }

        AfterSales afterSales = createAfterSales(dto, order, userId);
        afterSales.setStatus(AfterSales.STATUS_PENDING);
        this.save(afterSales);

        order.setStatus(5);
        orderService.updateById(order);
    }

    private AfterSales createAfterSales(AfterSalesApplyDto dto, Order order, Long userId) {
        AfterSales afterSales = new AfterSales();
        afterSales.setOrderNo(dto.getOrderNo());
        afterSales.setUserId(userId);
        afterSales.setType(dto.getType());
        afterSales.setReason(dto.getReason());
        afterSales.setDescription(dto.getDescription());
        
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                afterSales.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                log.error("序列化图片列表失败", e);
            }
        }
        
        afterSales.setRefundAmount(dto.getRefundAmount() != null ? dto.getRefundAmount() : order.getPayAmount());
        
        if (order.getReceiveTime() != null) {
            int earnedPoints = order.getPayAmount().multiply(new BigDecimal("100")).intValue();
            afterSales.setRefundPoints(earnedPoints);
        }
        
        return afterSales;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAfterSales(AfterSalesHandleDto dto) {
        AfterSales afterSales = this.getOne(new LambdaQueryWrapper<AfterSales>()
                .eq(AfterSales::getOrderNo, dto.getOrderNo())
                .eq(AfterSales::getStatus, AfterSales.STATUS_PENDING));
        
        if (afterSales == null) {
            throw new RuntimeException("找不到待处理的售后申请");
        }

        Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, dto.getOrderNo()));
        User user = userService.getById(order.getUserId());

        afterSales.setMerchantReply(dto.getReply());
        afterSales.setAdminRemark(dto.getAdminRemark());
        afterSales.setMerchantHandleTime(LocalDateTime.now());

        if (dto.getIsAgree()) {
            if (afterSales.getType() == AfterSales.TYPE_REFUND_ONLY) {
                processRefund(afterSales, order, user, dto.getRefundAmount());
            } else if (afterSales.getType() == AfterSales.TYPE_RETURN_REFUND) {
                afterSales.setStatus(AfterSales.STATUS_APPROVED);
            } else if (afterSales.getType() == AfterSales.TYPE_EXCHANGE) {
                afterSales.setStatus(AfterSales.STATUS_APPROVED);
            }
        } else {
            afterSales.setStatus(AfterSales.STATUS_REJECTED);
            order.setStatus(order.getDeliveryTime() == null ? 1 : (order.getReceiveTime() == null ? 2 : 3));
            user.setCreditScore(Math.max(0, user.getCreditScore() - 5));
            userService.updateById(user);
        }

        this.updateById(afterSales);
        orderService.updateById(order);
    }

    private void processRefund(AfterSales afterSales, Order order, User user, BigDecimal refundAmount) {
        afterSales.setStatus(AfterSales.STATUS_COMPLETED);
        afterSales.setRefundAmount(refundAmount != null ? refundAmount : order.getPayAmount());

        if (order.getReceiveTime() != null && afterSales.getRefundPoints() != null) {
            int pointsToDeduct = afterSales.getRefundPoints();
            if (user.getPoints() >= pointsToDeduct) {
                user.setPoints(user.getPoints() - pointsToDeduct);
            } else {
                int missingPoints = pointsToDeduct - user.getPoints();
                BigDecimal deductMoney = new BigDecimal(missingPoints).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                user.setPoints(0);
                afterSales.setRefundAmount(afterSales.getRefundAmount().subtract(deductMoney).max(BigDecimal.ZERO));
            }
            userService.updateById(user);
        }

        order.setStatus(4);
        order.setCloseTime(LocalDateTime.now());
        orderItemService.restoreInventoryByOrderId(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReturnLogistics(ReturnLogisticsDto dto, Long userId) {
        AfterSales afterSales = this.getOne(new LambdaQueryWrapper<AfterSales>()
                .eq(AfterSales::getOrderNo, dto.getOrderNo())
                .eq(AfterSales::getUserId, userId)
                .eq(AfterSales::getStatus, AfterSales.STATUS_APPROVED));

        if (afterSales == null) {
            throw new RuntimeException("找不到需要填写物流的售后申请");
        }

        afterSales.setReturnLogisticsCompany(dto.getLogisticsCompany());
        afterSales.setReturnLogisticsNo(dto.getLogisticsNo());
        afterSales.setReturnLogisticsTime(LocalDateTime.now());
        afterSales.setStatus(AfterSales.STATUS_RETURNING);

        this.updateById(afterSales);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturn(String orderNo, boolean isAgree, String remark) {
        AfterSales afterSales = this.getOne(new LambdaQueryWrapper<AfterSales>()
                .eq(AfterSales::getOrderNo, orderNo)
                .eq(AfterSales::getStatus, AfterSales.STATUS_RETURNING));

        if (afterSales == null) {
            throw new RuntimeException("找不到待确认退货的售后申请");
        }

        Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        User user = userService.getById(order.getUserId());

        if (isAgree) {
            processRefund(afterSales, order, user, afterSales.getRefundAmount());
        } else {
            afterSales.setStatus(AfterSales.STATUS_REJECTED);
            afterSales.setAdminRemark(remark);
            order.setStatus(order.getReceiveTime() == null ? 2 : 3);
        }

        this.updateById(afterSales);
        orderService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAfterSales(String orderNo, Long userId) {
        AfterSales afterSales = this.getOne(new LambdaQueryWrapper<AfterSales>()
                .eq(AfterSales::getOrderNo, orderNo)
                .eq(AfterSales::getUserId, userId)
                .eq(AfterSales::getStatus, AfterSales.STATUS_PENDING));

        if (afterSales == null) {
            throw new RuntimeException("只能撤销待审核的售后申请");
        }

        afterSales.setStatus(AfterSales.STATUS_CANCELLED);
        this.updateById(afterSales);

        Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        order.setStatus(order.getDeliveryTime() == null ? 1 : (order.getReceiveTime() == null ? 2 : 3));
        orderService.updateById(order);
    }

    @Override
    public List<AfterSales> getMyAfterSalesList(Long userId) {
        return this.list(new LambdaQueryWrapper<AfterSales>()
                .eq(AfterSales::getUserId, userId)
                .orderByDesc(AfterSales::getCreateTime));
    }

    @Override
    public List<AfterSales> getAllAfterSalesList() {
        return this.list(new LambdaQueryWrapper<AfterSales>()
                .orderByAsc(AfterSales::getStatus)
                .orderByDesc(AfterSales::getCreateTime));
    }
}
