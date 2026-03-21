package com.TsukasaChan.ShopVault.dto;

import lombok.Data;
import java.util.List;

@Data
public class YoloSearchResultDto {

    private boolean success;

    private String message;

    private List<String> detectedLabels;

    private List<CategoryInfo> matchedCategories;

    private List<CategoryInfo> hotCategories;

    @Data
    public static class CategoryInfo {

        private Long id;

        private String name;

        private String icon;

        private Integer productCount;

        public CategoryInfo(Long id, String name, String icon, Integer productCount) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.productCount = productCount;
        }
    }

    public static YoloSearchResultDto success(List<String> labels, List<CategoryInfo> categories) {
        YoloSearchResultDto dto = new YoloSearchResultDto();
        dto.setSuccess(true);
        dto.setMessage("识别成功");
        dto.setDetectedLabels(labels);
        dto.setMatchedCategories(categories);
        return dto;
    }

    public static YoloSearchResultDto noMatch(List<String> labels, List<CategoryInfo> hotCategories) {
        YoloSearchResultDto dto = new YoloSearchResultDto();
        dto.setSuccess(false);
        dto.setMessage("未识别到匹配的商品分类，请从热门分类中选择");
        dto.setDetectedLabels(labels);
        dto.setHotCategories(hotCategories);
        return dto;
    }

    public static YoloSearchResultDto noDetection(List<CategoryInfo> hotCategories) {
        YoloSearchResultDto dto = new YoloSearchResultDto();
        dto.setSuccess(false);
        dto.setMessage("未识别到商品，请从热门分类中选择或手动搜索");
        dto.setHotCategories(hotCategories);
        return dto;
    }
}
