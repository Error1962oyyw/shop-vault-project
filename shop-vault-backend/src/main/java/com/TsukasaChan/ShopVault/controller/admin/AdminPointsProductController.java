package com.TsukasaChan.ShopVault.controller.admin;

import com.TsukasaChan.ShopVault.common.PageResult;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.entity.marketing.PointsProduct;
import com.TsukasaChan.ShopVault.service.marketing.PointsProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/points-products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPointsProductController extends BaseController {

    private final PointsProductService pointsProductService;

    @GetMapping
    public Result<PageResult<PointsProduct>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type) {
        return Result.success(pointsProductService.getAdminPage(pageNum, pageSize, type));
    }

    @GetMapping("/{id}")
    public Result<PointsProduct> getById(@PathVariable Long id) {
        return Result.success(pointsProductService.getById(id));
    }

    @PostMapping
    public Result<String> save(@RequestBody PointsProduct product) {
        pointsProductService.save(product);
        return Result.success("添加成功");
    }

    @PutMapping
    public Result<String> update(@RequestBody PointsProduct product) {
        pointsProductService.updateById(product);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        pointsProductService.removeById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        PointsProduct product = pointsProductService.getById(id);
        if (product != null) {
            product.setStatus(status);
            pointsProductService.updateById(product);
        }
        return Result.success("状态更新成功");
    }

    @PutMapping("/{id}/stock")
    public Result<String> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        PointsProduct product = pointsProductService.getById(id);
        if (product != null) {
            product.setStock(stock);
            pointsProductService.updateById(product);
        }
        return Result.success("库存更新成功");
    }
}
