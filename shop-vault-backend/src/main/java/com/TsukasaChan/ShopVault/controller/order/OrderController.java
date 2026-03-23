package com.TsukasaChan.ShopVault.controller.order;

import com.TsukasaChan.ShopVault.annotation.LogOperation;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.dto.BuyNowDto;
import com.TsukasaChan.ShopVault.dto.CartCheckoutDto;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.service.order.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController extends BaseController {

    private final OrderService orderService;

    @GetMapping("/list")
    public Result<List<Order>> getOrderList() {
        Long userId = getCurrentUserId();
        List<Order> orders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime));
        return Result.success(orders);
    }

    @GetMapping("/detail/{orderNo}")
    public Result<Order> getOrderDetail(@PathVariable String orderNo) {
        Long userId = getCurrentUserId();
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));
        return Result.success(order);
    }

    @LogOperation(module = "订单交易", action = "直接下单购买")
    @PostMapping("/buy-now")
    public Result<String> buyNow(@RequestBody BuyNowDto dto) {
        String orderNo = orderService.buyNow(getCurrentUserId(), dto.getProductId(), dto.getQuantity(), dto.getUserCouponId());
        return Result.success("下单成功！单号：" + orderNo);
    }

    @LogOperation(module = "订单交易", action = "购物车批量结算")
    @PostMapping("/cart-checkout")
    public Result<String> cartCheckout(@RequestBody CartCheckoutDto dto) {
        String orderNo = orderService.cartCheckout(getCurrentUserId(), dto.getCartItemIds(), dto.getUserCouponId());
        return Result.success("购物车结算成功！单号：" + orderNo);
    }

    @PostMapping("/pay/{orderNo}")
    public Result<String> payOrder(@PathVariable String orderNo) {
        orderService.payOrder(orderNo, getCurrentUserId());
        return Result.success("支付成功！");
    }

    @LogOperation(module = "订单管理", action = "商家发货")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ship/{orderNo}")
    public Result<String> shipOrder(@PathVariable String orderNo) {
        orderService.shipOrder(orderNo);
        return Result.success("发货成功！");
    }

    @PostMapping("/receive/{orderNo}")
    public Result<String> receiveOrder(@PathVariable String orderNo) {
        orderService.confirmReceive(orderNo, getCurrentUserId());
        return Result.success("收货成功，100倍积分已到账！");
    }

    @PostMapping("/extend/{orderNo}")
    public Result<String> extendReceiveTime(@PathVariable String orderNo) {
        orderService.extendReceiveTime(orderNo, getCurrentUserId());
        return Result.success("已成功延长收货时间5天");
    }

    @LogOperation(module = "订单交易", action = "用户取消未付款订单")
    @PostMapping("/cancel/{orderNo}")
    public Result<String> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo, getCurrentUserId());
        return Result.success("订单已取消");
    }
}
