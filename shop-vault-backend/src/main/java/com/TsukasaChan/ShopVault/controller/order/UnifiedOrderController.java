package com.TsukasaChan.ShopVault.controller.order;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.common.VipConstants;
import com.TsukasaChan.ShopVault.dto.BuyNowDto;
import com.TsukasaChan.ShopVault.dto.CartCheckoutDto;
import com.TsukasaChan.ShopVault.dto.CreateOrderDto;
import com.TsukasaChan.ShopVault.dto.OrderDetailDto;
import com.TsukasaChan.ShopVault.entity.order.Order;
import com.TsukasaChan.ShopVault.service.order.UnifiedOrderService;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class UnifiedOrderController extends BaseController {

    private final UnifiedOrderService unifiedOrderService;

    public UnifiedOrderController(UnifiedOrderService unifiedOrderService) {
        this.unifiedOrderService = unifiedOrderService;
    }

    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@RequestBody CreateOrderDto dto) {
        Long userId = getCurrentUserId();
        Order order = unifiedOrderService.createOrder(userId, dto);
        return Result.success(buildOrderResult(order));
    }

    @PostMapping("/vip")
    public Result<Map<String, Object>> createVipOrder(@RequestBody Map<String, Object> params) {
        Long userId = getCurrentUserId();
        
        Object vipTypeObj = params.get("vipType");
        if (vipTypeObj == null || !(vipTypeObj instanceof Integer)) {
            return Result.error(400, "vipType参数类型错误或缺失");
        }
        int vipType = (Integer) vipTypeObj;
        
        if (vipType < VipConstants.TYPE_VIP_MONTHLY || vipType > VipConstants.TYPE_SVIP_YEARLY) {
            return Result.error(400, "vipType参数值无效，有效值: "
                    + VipConstants.TYPE_VIP_MONTHLY + "=VIP月卡, "
                    + VipConstants.TYPE_VIP_YEARLY + "=VIP年卡, "
                    + VipConstants.TYPE_SVIP_YEARLY + "=SVIP年卡");
        }
        
        Object paymentMethodObj = params.get("paymentMethod");
        if (paymentMethodObj == null || !(paymentMethodObj instanceof String)) {
            return Result.error(400, "paymentMethod参数类型错误或缺失");
        }
        String paymentMethod = (String) paymentMethodObj;
        
        Order order = unifiedOrderService.createVipOrder(userId, vipType, paymentMethod);
        return Result.success(buildOrderResult(order));
    }

    @PostMapping("/points-exchange")
    public Result<Map<String, Object>> createPointsExchangeOrder(@RequestBody Map<String, Object> params) {
        Long userId = getCurrentUserId();
        
        Object productIdObj = params.get("productId");
        if (productIdObj == null) {
            return Result.error(400, "productId参数不能为空");
        }
        Long productId;
        try {
            productId = Long.valueOf(productIdObj.toString());
        } catch (NumberFormatException e) {
            return Result.error(400, "productId参数格式错误");
        }
        
        Integer quantity = 1;
        if (params.get("quantity") != null) {
            Object quantityObj = params.get("quantity");
            if (quantityObj instanceof Integer) {
                quantity = (Integer) quantityObj;
            } else if (quantityObj instanceof Number) {
                quantity = ((Number) quantityObj).intValue();
            } else {
                try {
                    quantity = Integer.parseInt(quantityObj.toString());
                } catch (NumberFormatException e) {
                    return Result.error(400, "quantity参数格式错误");
                }
            }
        }
        
        Order order = unifiedOrderService.createPointsExchangeOrder(userId, productId, quantity);
        return Result.success(buildOrderResult(order));
    }

    @GetMapping
    public Result<IPage<OrderDetailDto>> getUserOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        return Result.success(unifiedOrderService.getUserOrders(userId, status, page, size, keyword));
    }

    @GetMapping("/{orderId}")
    public Result<OrderDetailDto> getOrderDetail(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        OrderDetailDto detail = unifiedOrderService.getOrderDetail(userId, orderId);
        if (detail == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(detail);
    }

    @PostMapping("/{orderId}/pay")
    public Result<Map<String, Object>> payOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> params) {
        Long userId = getCurrentUserId();
        String paymentMethod = params.get("paymentMethod");
        
        try {
            boolean success;
            if ("BALANCE".equals(paymentMethod)) {
                success = unifiedOrderService.payOrderByBalance(userId, orderId);
            } else if ("POINTS".equals(paymentMethod)) {
                success = unifiedOrderService.payOrderByPoints(userId, orderId);
            } else if ("DIRECT".equals(paymentMethod)) {
                success = unifiedOrderService.payOrderByDirect(userId, orderId);
            } else if ("COMBO".equals(paymentMethod)) {
                success = unifiedOrderService.payOrderByCombo(userId, orderId);
            } else {
                return Result.error(400, "不支持的支付方式");
            }
            
            if (success) {
                Order order = unifiedOrderService.getById(orderId);
                return Result.success(buildOrderResult(order));
            } else {
                return Result.error(500, "支付失败");
            }
        } catch (RuntimeException e) {
            log.error("支付失败, orderId={}, error={}", orderId, e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> params) {
        Long userId = getCurrentUserId();
        String reason = params != null ? params.get("reason") : "用户主动取消";
        
        try {
            unifiedOrderService.cancelOrder(userId, orderId, reason);
            return Result.success();
        } catch (RuntimeException e) {
            log.error("取消订单失败, orderId={}, error={}", orderId, e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{orderId}")
    public Result<Void> deleteOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        try {
            unifiedOrderService.deleteOrder(userId, orderId);
            return Result.success();
        } catch (RuntimeException e) {
            log.error("删除订单失败, orderId={}, error={}", orderId, e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/checkout")
    public Result<Map<String, Object>> cartCheckout(@RequestBody CartCheckoutDto dto) {
        Long userId = getCurrentUserId();
        try {
            Order order = unifiedOrderService.createOrderFromCart(userId, dto.getCartItemIds(), dto.getUserCouponId());
            return Result.success(buildOrderResult(order));
        } catch (RuntimeException e) {
            log.error("购物车结算失败, userId={}, error={}", userId, e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/buy-now")
    public Result<Map<String, Object>> buyNow(@RequestBody BuyNowDto dto) {
        Long userId = getCurrentUserId();
        try {
            Order order = unifiedOrderService.createBuyNowOrder(userId, dto);
            return Result.success(buildOrderResult(order));
        } catch (RuntimeException e) {
            log.error("直接购买失败, userId={}, error={}", userId, e.getMessage());
            return Result.error(400, e.getMessage());
        }
    }

    private Map<String, Object> buildOrderResult(Order order) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getStatus());
        result.put("totalAmount", order.getTotalAmount());
        result.put("payAmount", order.getPayAmount());
        result.put("pointsAmount", order.getPointsAmount());
        result.put("expireTime", order.getExpireTime());
        return result;
    }
}
