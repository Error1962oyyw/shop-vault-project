package com.TsukasaChan.ShopVault.service.system;

import com.TsukasaChan.ShopVault.entity.system.YoloMapping;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface YoloMappingService extends IService<YoloMapping> {

    List<Long> findCategoryIdsByLabels(List<String> labels);

    List<YoloMapping> listActiveMappings();

    List<String> getAvailableYoloLabels();
}
