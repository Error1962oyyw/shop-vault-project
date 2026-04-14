package com.TsukasaChan.ShopVault.controller.admin;

import com.TsukasaChan.ShopVault.common.PageResult;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController extends BaseController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Order>> getAdminOrderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(Order::getCreateTime, LocalDateTime.parse(startTime));
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(Order::getCreateTime, LocalDateTime.parse(endTime));
        }
        wrapper.orderByDesc(Order::getCreateTime);
        
        Page<Order> page = orderService.page(new Page<>(pageNum, pageSize), wrapper);
        
        PageResult<Order> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        
        return Result.success(result);
    }

    @GetMapping("/detail/{orderNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Order> getOrderDetail(@PathVariable String orderNo) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        return Result.success(order);
    }

    @PutMapping("/{orderNo}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateOrderStatus(
            @PathVariable String orderNo,
            @RequestParam Integer status
    ) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        order.setStatus(status);
        orderService.updateById(order);
        return Result.success("订单状态更新成功");
    }

    @PostMapping("/{orderNo}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> shipOrder(
            @PathVariable String orderNo,
            @RequestBody ShipRequest request
    ) {
        if (!StringUtils.hasText(request.getTrackingCompany())) {
            return Result.error(400, "请选择物流公司");
        }
        if (!StringUtils.hasText(request.getTrackingNo())) {
            return Result.error(400, "请输入物流单号");
        }
        if (request.getTrackingNo().length() > 50) {
            return Result.error(400, "物流单号长度不能超过50个字符");
        }

        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (order.getStatus() != Order.STATUS_PENDING_DELIVERY) {
            return Result.error(400, "当前订单状态不允许发货");
        }
        order.setStatus(Order.STATUS_PENDING_RECEIVE);
        order.setTrackingCompany(request.getTrackingCompany());
        order.setTrackingNo(request.getTrackingNo());
        order.setDeliveryTime(LocalDateTime.now());
        orderService.updateById(order);
        return Result.success("发货成功");
    }

    @DeleteMapping("/{orderNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteOrder(@PathVariable String orderNo) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        order.setIsDeleted(1);
        orderService.updateById(order);
        return Result.success("订单删除成功");
    }

    @lombok.Data
    public static class ShipRequest {
        private String trackingCompany;
        private String trackingNo;
    }
}
