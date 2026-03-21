package com.TsukasaChan.ShopVault.controller.product;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.dto.ProductSkuBatchDto;
import com.TsukasaChan.ShopVault.dto.SkuCreateDto;
import com.TsukasaChan.ShopVault.entity.product.ProductSku;
import com.TsukasaChan.ShopVault.service.product.ProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/skus")
@RequiredArgsConstructor
public class ProductSkuController {

    private final ProductSkuService productSkuService;

    @GetMapping("/product/{productId}")
    public Result<List<ProductSku>> listByProductId(@PathVariable Long productId) {
        return Result.success(productSkuService.listByProductId(productId));
    }

    @GetMapping("/{id}")
    public Result<ProductSku> getById(@PathVariable Long id) {
        return Result.success(productSkuService.getById(id));
    }

    @GetMapping("/code/{skuCode}")
    public Result<ProductSku> getBySkuCode(@PathVariable String skuCode) {
        return Result.success(productSkuService.getBySkuCode(skuCode));
    }

    @PostMapping
    public Result<ProductSku> create(@RequestBody SkuCreateDto dto) {
        ProductSku sku = buildSkuFromDto(dto.getProductId(), dto.getSkuCode(), dto.getSpecJson(), 
                dto.getPrice(), dto.getStock(), dto.getStockWarning(), dto.getImage());
        productSkuService.save(sku);
        return Result.success(sku);
    }

    @PostMapping("/batch")
    public Result<List<ProductSku>> createBatch(@RequestBody ProductSkuBatchDto dto) {
        productSkuService.deleteByProductId(dto.getProductId());
        
        for (ProductSkuBatchDto.SkuItem item : dto.getSkuList()) {
            ProductSku sku = buildSkuFromDto(dto.getProductId(), item.getSkuCode(), item.getSpecJson(), 
                    item.getPrice(), item.getStock(), item.getStockWarning(), item.getImage());
            productSkuService.save(sku);
        }
        
        return Result.success(productSkuService.listByProductId(dto.getProductId()));
    }

    private ProductSku buildSkuFromDto(Long productId, String skuCode, String specJson, 
            java.math.BigDecimal price, Integer stock, Integer stockWarning, String image) {
        ProductSku sku = new ProductSku();
        sku.setProductId(productId);
        sku.setSkuCode(skuCode);
        sku.setSpecJson(specJson);
        sku.setPrice(price);
        sku.setStock(stock != null ? stock : 0);
        sku.setStockWarning(stockWarning != null ? stockWarning : 10);
        sku.setImage(image);
        sku.setStatus(1);
        sku.setCreateTime(LocalDateTime.now());
        sku.setUpdateTime(LocalDateTime.now());
        return sku;
    }

    @PutMapping("/{id}")
    public Result<ProductSku> update(@PathVariable Long id, @RequestBody SkuCreateDto dto) {
        ProductSku sku = productSkuService.getById(id);
        if (sku == null) {
            return Result.error("SKU不存在");
        }
        
        if (dto.getPrice() != null) {
            sku.setPrice(dto.getPrice());
        }
        if (dto.getStock() != null) {
            sku.setStock(dto.getStock());
        }
        if (dto.getStockWarning() != null) {
            sku.setStockWarning(dto.getStockWarning());
        }
        if (dto.getImage() != null) {
            sku.setImage(dto.getImage());
        }
        sku.setUpdateTime(LocalDateTime.now());
        productSkuService.updateById(sku);
        return Result.success(sku);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productSkuService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/product/{productId}")
    public Result<Void> deleteByProductId(@PathVariable Long productId) {
        productSkuService.deleteByProductId(productId);
        return Result.success();
    }

    @PostMapping("/{id}/stock/deduct")
    public Result<Boolean> deductStock(@PathVariable Long id, @RequestParam Integer quantity) {
        return Result.success(productSkuService.deductStock(id, quantity));
    }

    @PostMapping("/{id}/stock/add")
    public Result<Boolean> addStock(@PathVariable Long id, @RequestParam Integer quantity) {
        return Result.success(productSkuService.addStock(id, quantity));
    }
}
