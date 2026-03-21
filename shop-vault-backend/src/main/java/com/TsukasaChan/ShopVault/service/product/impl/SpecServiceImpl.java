package com.TsukasaChan.ShopVault.service.product.impl;

import com.TsukasaChan.ShopVault.entity.product.Spec;
import com.TsukasaChan.ShopVault.entity.product.SpecValue;
import com.TsukasaChan.ShopVault.mapper.product.SpecMapper;
import com.TsukasaChan.ShopVault.service.product.SpecService;
import com.TsukasaChan.ShopVault.service.product.SpecValueService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecServiceImpl extends ServiceImpl<SpecMapper, Spec> implements SpecService {

    private final SpecValueService specValueService;

    @Override
    public List<Spec> listWithValues() {
        List<Spec> specs = list(new LambdaQueryWrapper<Spec>().orderByAsc(Spec::getSortOrder));
        if (specs.isEmpty()) {
            return specs;
        }
        
        List<Long> specIds = specs.stream().map(Spec::getId).collect(Collectors.toList());
        List<SpecValue> allValues = specValueService.list(new LambdaQueryWrapper<SpecValue>()
                .in(SpecValue::getSpecId, specIds)
                .orderByAsc(SpecValue::getSortOrder));
        
        Map<Long, List<SpecValue>> valueMap = allValues.stream()
                .collect(Collectors.groupingBy(SpecValue::getSpecId));
        
        specs.forEach(spec -> spec.setValues(valueMap.getOrDefault(spec.getId(), List.of())));
        
        return specs;
    }

    @Override
    public Spec getByIdWithValues(Long id) {
        Spec spec = getById(id);
        if (spec != null) {
            List<SpecValue> values = specValueService.listBySpecId(id);
            spec.setValues(values);
        }
        return spec;
    }
}
