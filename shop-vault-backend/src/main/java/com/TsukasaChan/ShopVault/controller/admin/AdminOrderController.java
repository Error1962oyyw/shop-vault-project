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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order/admin")
@RequiredArgsConstructor
public class AdminOrderController extends BaseController {

    private final OrderService orderService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Order>> getAdminOrderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status
    ) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
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
}
