package com.TsukasaChan.ShopVault.controller.admin;

import com.TsukasaChan.ShopVault.common.PageResult;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.entity.order.OrderItem;
import com.TsukasaChan.ShopVault.entity.system.Address;
import com.TsukasaChan.ShopVault.service.order.OrderItemService;
import com.TsukasaChan.ShopVault.service.order.OrderLifecycleService;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.TsukasaChan.ShopVault.service.system.AddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController extends BaseController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderLifecycleService orderLifecycleService;
    private final AddressService addressService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Order>> getAdminOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
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
        
        Page<Order> pageResult = orderService.page(new Page<>(page, size), wrapper);

        List<Order> records = pageResult.getRecords();
        if (!records.isEmpty()) {
            List<Long> orderIds = records.stream().map(Order::getId).toList();
            Map<Long, OrderItem> itemMap = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                    .in(OrderItem::getOrderId, orderIds))
                    .stream().collect(Collectors.toMap(OrderItem::getOrderId, Function.identity(), (a, b) -> a));

            List<Long> addressIds = records.stream()
                    .map(Order::getAddressId)
                    .filter(id -> id != null)
                    .distinct().toList();
            Map<Long, Address> addressMap = addressIds.isEmpty() ? Map.of()
                    : addressService.listByIds(addressIds).stream()
                    .collect(Collectors.toMap(Address::getId, Function.identity()));

            for (Order order : records) {
                if (order.getProductName() == null) {
                    OrderItem item = itemMap.get(order.getId());
                    if (item != null) {
                        order.setProductName(item.getProductName());
                        order.setProductImage(item.getProductImg());
                    }
                }
                if (order.getAddressId() != null && order.getReceiverSnapshot() == null) {
                    Address addr = addressMap.get(order.getAddressId());
                    if (addr != null) {
                        order.setReceiverSnapshot(
                            "{\"receiverName\":\"" + addr.getReceiverName() + "\","
                            + "\"receiverPhone\":\"" + addr.getReceiverPhone() + "\","
                            + "\"address\":\"" + addr.getProvince() + addr.getCity() + addr.getRegion() + addr.getDetailAddress() + "\"}"
                        );
                    }
                }
            }
        }
        
        PageResult<Order> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(pageResult.getTotal());
        result.setCurrent(pageResult.getCurrent());
        result.setSize(pageResult.getSize());
        
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

        orderLifecycleService.shipOrder(orderNo);

        Order updatedOrder = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (updatedOrder != null) {
            updatedOrder.setTrackingCompany(request.getTrackingCompany());
            updatedOrder.setTrackingNo(request.getTrackingNo());
            orderService.updateById(updatedOrder);
        }

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
        orderItemService.remove(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        orderService.removeById(order.getId());
        return Result.success("订单删除成功");
    }

    @lombok.Data
    public static class ShipRequest {
        private String trackingCompany;
        private String trackingNo;
    }
}
