package com.TsukasaChan.ShopVault.controller.product;

import com.TsukasaChan.ShopVault.annotation.LogOperation;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.common.SecurityUtils;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.dto.YoloSearchResultDto;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.integration.YoloClientService;
import com.TsukasaChan.ShopVault.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController extends BaseController {

    private final ProductService productService;
    private final YoloClientService yoloClientService;

    @LogOperation(module = "商品管理", action = "发布新商品")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/publish")
    public Result<String> publishProduct(@RequestBody Product product) {
        if (!StringUtils.hasText(product.getName()) || product.getPrice() == null) {
            return Result.error(400, "商品名称和价格不能为空");
        }
        product.setStatus(1);
        product.setSales(0);
        productService.save(product);
        return Result.success("商品发布成功！");
    }

    @GetMapping("/list")
    public Result<?> getProductList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(productService.getProductPage(current, size, keyword, categoryId));
    }

    @PostMapping("/yolo-search")
    public Result<YoloSearchResultDto> yoloSearch(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请上传图片");
        }

        List<String> labels = yoloClientService.detectImage(file);
        YoloSearchResultDto result = productService.yoloSearch(labels);
        return Result.success(result);
    }

    @GetMapping("/hot-categories")
    public Result<List<YoloSearchResultDto.CategoryInfo>> getHotCategoriesApi() {
        return Result.success(productService.getHotCategories());
    }

    @GetMapping("/detail/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetailWithBehavior(id, SecurityUtils.getCurrentUsername()));
    }
}
