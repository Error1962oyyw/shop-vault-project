package com.TsukasaChan.ShopVault.controller.product;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.dto.SpecCreateDto;
import com.TsukasaChan.ShopVault.entity.product.Spec;
import com.TsukasaChan.ShopVault.entity.product.SpecValue;
import com.TsukasaChan.ShopVault.service.product.SpecService;
import com.TsukasaChan.ShopVault.service.product.SpecValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/specs")
@RequiredArgsConstructor
public class SpecController {

    private final SpecService specService;
    private final SpecValueService specValueService;

    @GetMapping
    public Result<List<Spec>> list() {
        return Result.success(specService.listWithValues());
    }

    @GetMapping("/{id}")
    public Result<Spec> getById(@PathVariable Long id) {
        return Result.success(specService.getByIdWithValues(id));
    }

    @PostMapping
    public Result<Spec> create(@RequestBody SpecCreateDto dto) {
        Spec spec = new Spec();
        spec.setName(dto.getName());
        spec.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        spec.setCreateTime(LocalDateTime.now());
        specService.save(spec);
        
        if (dto.getValues() != null && !dto.getValues().isEmpty()) {
            int order = 0;
            for (String value : dto.getValues()) {
                SpecValue specValue = new SpecValue();
                specValue.setSpecId(spec.getId());
                specValue.setValue(value);
                specValue.setSortOrder(order++);
                specValue.setCreateTime(LocalDateTime.now());
                specValueService.save(specValue);
            }
        }
        
        return Result.success(specService.getByIdWithValues(spec.getId()));
    }

    @PutMapping("/{id}")
    public Result<Spec> update(@PathVariable Long id, @RequestBody SpecCreateDto dto) {
        Spec spec = specService.getById(id);
        if (spec == null) {
            return Result.error("规格不存在");
        }
        
        spec.setName(dto.getName());
        if (dto.getSortOrder() != null) {
            spec.setSortOrder(dto.getSortOrder());
        }
        specService.updateById(spec);
        
        specValueService.deleteBySpecId(id);
        if (dto.getValues() != null) {
            int order = 0;
            for (String value : dto.getValues()) {
                SpecValue specValue = new SpecValue();
                specValue.setSpecId(id);
                specValue.setValue(value);
                specValue.setSortOrder(order++);
                specValue.setCreateTime(LocalDateTime.now());
                specValueService.save(specValue);
            }
        }
        
        return Result.success(specService.getByIdWithValues(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        specValueService.deleteBySpecId(id);
        specService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}/values")
    public Result<List<SpecValue>> listValues(@PathVariable Long id) {
        return Result.success(specValueService.listBySpecId(id));
    }

    @PostMapping("/{id}/values")
    public Result<SpecValue> addValue(@PathVariable Long id, @RequestBody SpecValue specValue) {
        specValue.setSpecId(id);
        specValue.setCreateTime(LocalDateTime.now());
        if (specValue.getSortOrder() == null) {
            specValue.setSortOrder(0);
        }
        specValueService.save(specValue);
        return Result.success(specValue);
    }

    @DeleteMapping("/values/{valueId}")
    public Result<Void> deleteValue(@PathVariable Long valueId) {
        specValueService.removeById(valueId);
        return Result.success();
    }
}
