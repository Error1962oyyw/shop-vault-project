package com.TsukasaChan.ShopVault.controller.product;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.dto.ProductSpecDto;
import com.TsukasaChan.ShopVault.dto.ProductSpecValueDto;
import com.TsukasaChan.ShopVault.entity.product.ProductSpec;
import com.TsukasaChan.ShopVault.entity.product.ProductSpecValue;
import com.TsukasaChan.ShopVault.entity.product.Spec;
import com.TsukasaChan.ShopVault.entity.product.SpecValue;
import com.TsukasaChan.ShopVault.service.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/product-specs")
@RequiredArgsConstructor
public class ProductSpecController {

    private final ProductSpecService productSpecService;
    private final ProductSpecValueService productSpecValueService;
    private final SpecService specService;
    private final SpecValueService specValueService;

    @GetMapping("/product/{productId}")
    public Result<List<ProductSpecDto>> listByProduct(@PathVariable Long productId) {
        List<ProductSpec> productSpecs = productSpecService.listByProductId(productId);
        
        if (productSpecs.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<Long> productSpecIds = productSpecs.stream()
                .map(ProductSpec::getId)
                .collect(Collectors.toList());

        List<ProductSpecValue> allValues = productSpecValueService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductSpecValue>()
                        .in(ProductSpecValue::getProductSpecId, productSpecIds)
                        .orderByAsc(ProductSpecValue::getSortOrder));

        Map<Long, List<ProductSpecValue>> valueMap = allValues.stream()
                .collect(Collectors.groupingBy(ProductSpecValue::getProductSpecId));

        List<ProductSpecDto> result = productSpecs.stream().map(ps -> {
            ProductSpecDto dto = new ProductSpecDto();
            dto.setId(ps.getId());
            dto.setSpecId(ps.getSpecId());
            dto.setSpecName(ps.getSpecName());
            dto.setSortOrder(ps.getSortOrder());
            dto.setIsCustom(ps.getIsCustom());

            List<ProductSpecValue> values = valueMap.getOrDefault(ps.getId(), new ArrayList<>());
            dto.setValues(values.stream().map(v -> {
                ProductSpecValueDto vDto = new ProductSpecValueDto();
                vDto.setId(v.getId());
                vDto.setSpecValueId(v.getSpecValueId());
                vDto.setValue(v.getValue());
                vDto.setSortOrder(v.getSortOrder());
                vDto.setIsCustom(v.getIsCustom());
                return vDto;
            }).collect(Collectors.toList()));

            return dto;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @PostMapping("/product/{productId}")
    @Transactional(rollbackFor = Exception.class)
    public Result<ProductSpecDto> addSpecToProduct(@PathVariable Long productId, @RequestBody ProductSpecDto dto) {
        if (dto.getSpecName() == null || dto.getSpecName().trim().isEmpty()) {
            return Result.error(400, "规格名称不能为空");
        }

        ProductSpec existingSpec = productSpecService.getByProductIdAndSpecId(productId, 
                dto.getSpecId() != null ? dto.getSpecId() : 0L);
        if (existingSpec != null && dto.getSpecId() != null && dto.getSpecId() > 0) {
            return Result.error(400, "该商品已关联此规格");
        }

        ProductSpec productSpec = new ProductSpec();
        productSpec.setProductId(productId);
        productSpec.setSpecId(dto.getSpecId() != null ? dto.getSpecId() : 0L);
        productSpec.setSpecName(dto.getSpecName().trim());
        productSpec.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        productSpec.setIsCustom(dto.getIsCustom() != null ? dto.getIsCustom() : 
                ((dto.getSpecId() == null || dto.getSpecId() == 0L) ? 1 : 0));
        productSpec.setCreateTime(LocalDateTime.now());
        productSpecService.save(productSpec);

        if (dto.getValues() != null && !dto.getValues().isEmpty()) {
            int order = 0;
            for (ProductSpecValueDto valueDto : dto.getValues()) {
                if (valueDto.getValue() == null || valueDto.getValue().trim().isEmpty()) {
                    continue;
                }

                if (productSpecValueService.existsByValue(productSpec.getId(), valueDto.getValue().trim())) {
                    continue;
                }

                ProductSpecValue value = new ProductSpecValue();
                value.setProductSpecId(productSpec.getId());
                value.setSpecValueId(valueDto.getSpecValueId());
                value.setValue(valueDto.getValue().trim());
                value.setSortOrder(valueDto.getSortOrder() != null ? valueDto.getSortOrder() : order++);
                value.setIsCustom(valueDto.getIsCustom() != null ? valueDto.getIsCustom() : 
                        (valueDto.getSpecValueId() == null ? 1 : 0));
                value.setCreateTime(LocalDateTime.now());
                productSpecValueService.save(value);
            }
        }

        dto.setId(productSpec.getId());
        return Result.success(dto);
    }

    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<ProductSpecDto> updateSpec(@PathVariable Long id, @RequestBody ProductSpecDto dto) {
        ProductSpec productSpec = productSpecService.getById(id);
        if (productSpec == null) {
            return Result.error(404, "规格不存在");
        }

        if (dto.getSpecName() != null && !dto.getSpecName().trim().isEmpty()) {
            productSpec.setSpecName(dto.getSpecName().trim());
        }
        if (dto.getSortOrder() != null) {
            productSpec.setSortOrder(dto.getSortOrder());
        }
        productSpecService.updateById(productSpec);

        if (dto.getValues() != null) {
            List<ProductSpecValue> existingValues = productSpecValueService.listByProductSpecId(id);
            Set<Long> dtoValueIds = dto.getValues().stream()
                    .map(ProductSpecValueDto::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            
            for (ProductSpecValue existingValue : existingValues) {
                if (!dtoValueIds.contains(existingValue.getId())) {
                    productSpecValueService.removeById(existingValue.getId());
                }
            }

            int order = 0;
            for (ProductSpecValueDto valueDto : dto.getValues()) {
                if (valueDto.getValue() == null || valueDto.getValue().trim().isEmpty()) {
                    continue;
                }

                if (valueDto.getId() != null) {
                    ProductSpecValue existingValue = productSpecValueService.getById(valueDto.getId());
                    if (existingValue != null) {
                        existingValue.setValue(valueDto.getValue().trim());
                        existingValue.setSortOrder(valueDto.getSortOrder() != null ? valueDto.getSortOrder() : order++);
                        productSpecValueService.updateById(existingValue);
                        continue;
                    }
                }

                if (productSpecValueService.existsByValue(id, valueDto.getValue().trim())) {
                    continue;
                }

                ProductSpecValue value = new ProductSpecValue();
                value.setProductSpecId(id);
                value.setSpecValueId(valueDto.getSpecValueId());
                value.setValue(valueDto.getValue().trim());
                value.setSortOrder(valueDto.getSortOrder() != null ? valueDto.getSortOrder() : order++);
                value.setIsCustom(valueDto.getIsCustom() != null ? valueDto.getIsCustom() : 
                        (valueDto.getSpecValueId() == null ? 1 : 0));
                value.setCreateTime(LocalDateTime.now());
                productSpecValueService.save(value);
            }
        }

        return Result.success(dto);
    }

    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteSpec(@PathVariable Long id) {
        productSpecValueService.deleteByProductSpecId(id);
        productSpecService.removeById(id);
        return Result.success();
    }

    @PostMapping("/{productSpecId}/values")
    @Transactional(rollbackFor = Exception.class)
    public Result<ProductSpecValueDto> addValue(@PathVariable Long productSpecId, @RequestBody ProductSpecValueDto dto) {
        ProductSpec productSpec = productSpecService.getById(productSpecId);
        if (productSpec == null) {
            return Result.error(404, "商品规格不存在");
        }

        if (dto.getValue() == null || dto.getValue().trim().isEmpty()) {
            return Result.error(400, "规格值不能为空");
        }

        if (dto.getValue().length() > 50) {
            return Result.error(400, "规格值长度不能超过50个字符");
        }

        if (productSpecValueService.existsByValue(productSpecId, dto.getValue().trim())) {
            return Result.error(400, "该规格值已存在");
        }

        ProductSpecValue value = new ProductSpecValue();
        value.setProductSpecId(productSpecId);
        value.setSpecValueId(dto.getSpecValueId());
        value.setValue(dto.getValue().trim());
        value.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        value.setIsCustom(dto.getIsCustom() != null ? dto.getIsCustom() : 
                (dto.getSpecValueId() == null) ? 1 : 0);
        value.setCreateTime(LocalDateTime.now());
        productSpecValueService.save(value);

        dto.setId(value.getId());
        return Result.success(dto);
    }

    @PutMapping("/values/{valueId}")
    public Result<ProductSpecValueDto> updateValue(@PathVariable Long valueId, @RequestBody ProductSpecValueDto dto) {
        ProductSpecValue value = productSpecValueService.getById(valueId);
        if (value == null) {
            return Result.error(404, "规格值不存在");
        }

        if (dto.getValue() != null && !dto.getValue().trim().isEmpty()) {
            if (dto.getValue().length() > 50) {
                return Result.error(400, "规格值长度不能超过50个字符");
            }

            if (!value.getValue().equals(dto.getValue().trim()) &&
                    productSpecValueService.existsByValue(value.getProductSpecId(), dto.getValue().trim())) {
                return Result.error(400, "该规格值已存在");
            }
            value.setValue(dto.getValue().trim());
        }

        if (dto.getSortOrder() != null) {
            value.setSortOrder(dto.getSortOrder());
        }
        productSpecValueService.updateById(value);

        dto.setId(value.getId());
        return Result.success(dto);
    }

    @DeleteMapping("/values/{valueId}")
    public Result<Void> deleteValue(@PathVariable Long valueId) {
        productSpecValueService.removeById(valueId);
        return Result.success();
    }

    @GetMapping("/templates")
    public Result<List<Spec>> listTemplates() {
        return Result.success(specService.listWithValues());
    }

    @GetMapping("/templates/{specId}/values")
    public Result<List<SpecValue>> listTemplateValues(@PathVariable Long specId) {
        return Result.success(specValueService.listBySpecId(specId));
    }
}
