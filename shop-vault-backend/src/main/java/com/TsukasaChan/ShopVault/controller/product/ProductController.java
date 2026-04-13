package com.TsukasaChan.ShopVault.controller.product;

import com.TsukasaChan.ShopVault.annotation.LogOperation;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.common.SecurityUtils;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.dto.YoloSearchResultDto;
import com.TsukasaChan.ShopVault.entity.product.Category;
import com.TsukasaChan.ShopVault.entity.product.Product;
import com.TsukasaChan.ShopVault.integration.YoloClientService;
import com.TsukasaChan.ShopVault.service.product.CategoryService;
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
    private final CategoryService categoryService;
    private final YoloClientService yoloClientService;

    @LogOperation(module = "商品管理", action = "发布新商品")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/publish")
    public Result<String> publishProduct(@RequestBody Product product) {
        if (!StringUtils.hasText(product.getName()) || product.getPrice() == null) {
            return Result.error(400, "商品名称和价格不能为空");
        }
        if (product.getCategoryId() == null) {
            return Result.error(400, "请选择商品分类");
        }
        Category category = categoryService.getById(product.getCategoryId());
        if (category == null || category.getLevel() != 2) {
            return Result.error(400, "请选择有效的商品小类");
        }
        if (!StringUtils.hasText(product.getMainImage())) {
            product.setMainImage("/images/default-product.png");
        }
        product.setStatus(1);
        product.setSales(0);
        productService.saveProductWithCategory(product);
        return Result.success("商品发布成功！");
    }

    @GetMapping("/list")
    public Result<?> getProductList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "default") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        return Result.success(productService.getProductPageWithCategory(pageNum, pageSize, keyword, categoryId, sortBy, sortOrder));
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
        return Result.success(productService.getProductDetailWithCategory(id, SecurityUtils.getCurrentUsername()));
    }
}
