package com.TsukasaChan.ShopVault.service.system.impl;

import com.TsukasaChan.ShopVault.entity.system.YoloMapping;
import com.TsukasaChan.ShopVault.mapper.system.YoloMappingMapper;
import com.TsukasaChan.ShopVault.service.system.YoloMappingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class YoloMappingServiceImpl extends ServiceImpl<YoloMappingMapper, YoloMapping> implements YoloMappingService {

    private static final Set<String> COCO_LABELS = Set.of(
            "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
            "traffic light", "fire hydrant", "stop sign", "parking meter", "bench",
            "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe",
            "backpack", "umbrella", "handbag", "tie", "suitcase",
            "frisbee", "skis", "snowboard", "sports ball", "kite",
            "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
            "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl",
            "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake",
            "chair", "couch", "potted plant", "bed", "dining table", "toilet",
            "tv", "laptop", "mouse", "remote", "keyboard", "cell phone",
            "microwave", "oven", "toaster", "sink", "refrigerator",
            "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"
    );

    @Override
    public List<Long> findCategoryIdsByLabels(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return List.of();
        }

        List<YoloMapping> mappings = this.list(new LambdaQueryWrapper<YoloMapping>()
                .in(YoloMapping::getYoloLabel, labels)
                .eq(YoloMapping::getIsActive, true));

        return mappings.stream()
                .map(YoloMapping::getCategoryId)
                .distinct()
                .toList();
    }

    @Override
    public List<YoloMapping> listActiveMappings() {
        return this.list(new LambdaQueryWrapper<YoloMapping>()
                .eq(YoloMapping::getIsActive, true));
    }

    @Override
    public List<String> getAvailableYoloLabels() {
        List<String> configuredLabels = this.list().stream()
                .map(YoloMapping::getYoloLabel)
                .distinct()
                .toList();
        
        return COCO_LABELS.stream()
                .filter(label -> !configuredLabels.contains(label))
                .toList();
    }
}
