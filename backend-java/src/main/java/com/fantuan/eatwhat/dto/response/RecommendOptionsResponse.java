package com.fantuan.eatwhat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推荐选项元数据响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendOptionsResponse {

    private List<OptionItem> mealTypes;
    private List<OptionItem> priceLevels;
    private List<OptionItem> tastes;
    private List<OptionItem> typeTags;
    private List<OptionItem> cuisineTags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionItem {
        private String value;
        private String label;
        private String hint;
        private int sortOrder;
    }
}
