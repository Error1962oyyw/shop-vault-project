package com.TsukasaChan.ShopVault.controller.admin;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.dto.AfterSalesHandleDto;
import com.TsukasaChan.ShopVault.entity.order.AfterSales;
import com.TsukasaChan.ShopVault.service.order.AfterSalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/after-sales")
@RequiredArgsConstructor
public class AdminAfterSalesController extends BaseController {

    private final AfterSalesService afterSalesService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<AfterSales>> getAdminAfterSalesList(
            @RequestParam(required = false) Integer status
    ) {
        List<AfterSales> list = afterSalesService.getAfterSalesByStatus(status);
        return Result.success(list);
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> resolveAfterSales(
            @PathVariable Long id,
            @RequestBody ResolveRequest request
    ) {
        AfterSales afterSales = afterSalesService.getById(id);
        if (afterSales == null) {
            return Result.error(404, "售后记录不存在");
        }
        if (afterSales.getStatus() != AfterSales.STATUS_PENDING) {
            return Result.error(400, "当前售后状态不允许此操作");
        }

        AfterSalesHandleDto dto = new AfterSalesHandleDto();
        dto.setOrderNo(afterSales.getOrderNo());
        dto.setIsAgree(request.getStatus() == 1);
        if (request.getRefundAmount() != null) {
            dto.setRefundAmount(request.getRefundAmount());
        }
        dto.setAdminRemark(request.getStatus() == 1 ? "同意售后申请" : "拒绝售后申请");

        afterSalesService.handleAfterSales(dto);
        return Result.success(request.getStatus() == 1 ? "已同意售后申请" : "已拒绝售后申请");
    }

    @lombok.Data
    public static class ResolveRequest {
        private Integer status;
        private java.math.BigDecimal refundAmount;
    }
}
