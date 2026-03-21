package com.TsukasaChan.ShopVault.controller.order;

import com.TsukasaChan.ShopVault.annotation.LogOperation;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.dto.AfterSalesApplyDto;
import com.TsukasaChan.ShopVault.dto.AfterSalesHandleDto;
import com.TsukasaChan.ShopVault.dto.ReturnLogisticsDto;
import com.TsukasaChan.ShopVault.entity.order.AfterSales;
import com.TsukasaChan.ShopVault.service.order.AfterSalesService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/after-sales")
@RequiredArgsConstructor
public class AfterSalesController extends BaseController {

    private final AfterSalesService afterSalesService;

    @LogOperation(module = "售后服务", action = "用户提交售后申请")
    @PostMapping("/apply")
    public Result<String> applyAfterSales(@RequestBody AfterSalesApplyDto dto) {
        if (dto.getOrderNo() == null || dto.getReason() == null) {
            return Result.error(400, "订单号和售后原因不能为空");
        }
        afterSalesService.applyAfterSales(dto, getCurrentUserId());
        return Result.success("售后申请已提交，请等待商家处理");
    }

    @GetMapping("/my-list")
    public Result<List<AfterSales>> getMyAfterSalesList() {
        return Result.success(afterSalesService.getMyAfterSalesList(getCurrentUserId()));
    }

    @LogOperation(module = "售后服务", action = "用户撤销售后申请")
    @PostMapping("/cancel")
    public Result<String> cancelAfterSales(@RequestParam String orderNo) {
        afterSalesService.cancelAfterSales(orderNo, getCurrentUserId());
        return Result.success("售后申请已撤销");
    }

    @LogOperation(module = "售后服务", action = "用户填写退货物流")
    @PostMapping("/return-logistics")
    public Result<String> submitReturnLogistics(@RequestBody ReturnLogisticsDto dto) {
        afterSalesService.submitReturnLogistics(dto, getCurrentUserId());
        return Result.success("退货物流信息已提交");
    }

    @LogOperation(module = "售后服务", action = "商家审核售后申请")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/handle")
    public Result<String> handleAfterSales(@RequestBody AfterSalesHandleDto dto) {
        if (dto.getOrderNo() == null || dto.getIsAgree() == null) {
            return Result.error(400, "处理参数不完整");
        }
        afterSalesService.handleAfterSales(dto);
        return Result.success("售后处理完毕：" + (dto.getIsAgree() ? "已同意" : "已拒绝"));
    }

    @LogOperation(module = "售后服务", action = "商家确认退货")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/confirm-return")
    public Result<String> confirmReturn(
            @RequestParam String orderNo,
            @RequestParam boolean isAgree,
            @RequestParam(required = false) String remark) {
        afterSalesService.confirmReturn(orderNo, isAgree, remark);
        return Result.success(isAgree ? "已确认退货并退款" : "已拒绝退货");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-list")
    public Result<List<AfterSales>> getAllAfterSalesList() {
        return Result.success(afterSalesService.getAllAfterSalesList());
    }

    @GetMapping("/detail/{orderNo}")
    public Result<AfterSales> getAfterSalesDetail(@PathVariable String orderNo) {
        AfterSales afterSales = afterSalesService.getOne(
                new LambdaQueryWrapper<AfterSales>()
                        .eq(AfterSales::getOrderNo, orderNo));
        if (afterSales == null) {
            return Result.error(404, "售后记录不存在");
        }
        if (!afterSales.getUserId().equals(getCurrentUserId())) {
            return Result.error(403, "无权查看此售后记录");
        }
        return Result.success(afterSales);
    }
}
