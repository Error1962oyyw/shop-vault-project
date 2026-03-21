package com.TsukasaChan.ShopVault.controller.system;

import com.TsukasaChan.ShopVault.annotation.LogOperation;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.entity.product.Category;
import com.TsukasaChan.ShopVault.entity.system.YoloMapping;
import com.TsukasaChan.ShopVault.service.product.CategoryService;
import com.TsukasaChan.ShopVault.service.system.YoloMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/yolo-mapping")
@RequiredArgsConstructor
public class YoloMappingController {

    private final YoloMappingService yoloMappingService;
    private final CategoryService categoryService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<YoloMapping>> listAll() {
        return Result.success(yoloMappingService.list());
    }

    @GetMapping("/active")
    public Result<List<YoloMapping>> listActive() {
        return Result.success(yoloMappingService.listActiveMappings());
    }

    @GetMapping("/available-labels")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<String>> getAvailableYoloLabels() {
        return Result.success(yoloMappingService.getAvailableYoloLabels());
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(module = "YOLO配置", action = "新增标签映射")
    public Result<String> addMapping(@RequestBody YoloMapping mapping) {
        if (mapping.getYoloLabel() == null || mapping.getYoloLabel().isBlank()) {
            return Result.error(400, "YOLO标签不能为空");
        }
        if (mapping.getCategoryId() == null) {
            return Result.error(400, "请选择关联的商品分类");
        }
        Category category = categoryService.getById(mapping.getCategoryId());
        if (category == null) {
            return Result.error(400, "选择的分类不存在");
        }
        if (mapping.getIsActive() == null) {
            mapping.setIsActive(true);
        }
        yoloMappingService.save(mapping);
        return Result.success("映射添加成功");
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(module = "YOLO配置", action = "更新标签映射")
    public Result<String> updateMapping(@RequestBody YoloMapping mapping) {
        if (mapping.getId() == null) {
            return Result.error(400, "映射ID不能为空");
        }
        YoloMapping existing = yoloMappingService.getById(mapping.getId());
        if (existing == null) {
            return Result.error(404, "映射记录不存在");
        }
        if (mapping.getCategoryId() != null) {
            Category category = categoryService.getById(mapping.getCategoryId());
            if (category == null) {
                return Result.error(400, "选择的分类不存在");
            }
        }
        yoloMappingService.updateById(mapping);
        return Result.success("映射更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(module = "YOLO配置", action = "删除标签映射")
    public Result<String> deleteMapping(@PathVariable Long id) {
        yoloMappingService.removeById(id);
        return Result.success("映射删除成功");
    }

    @PutMapping("/toggle/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @LogOperation(module = "YOLO配置", action = "切换映射状态")
    public Result<String> toggleMapping(@PathVariable Long id) {
        YoloMapping mapping = yoloMappingService.getById(id);
        if (mapping == null) {
            return Result.error(404, "映射记录不存在");
        }
        mapping.setIsActive(!mapping.getIsActive());
        yoloMappingService.updateById(mapping);
        return Result.success(mapping.getIsActive() ? "已启用" : "已禁用");
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Category>> getAllCategories() {
        return Result.success(categoryService.list());
    }
}
