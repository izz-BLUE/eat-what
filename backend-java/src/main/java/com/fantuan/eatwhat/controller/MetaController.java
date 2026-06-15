package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.common.RecommendDict;
import com.fantuan.eatwhat.dto.response.RecommendOptionsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 元数据控制器
 * 提供推荐维度的选项元数据，前端用于渲染筛选 UI
 */
@RestController
@RequestMapping("/api/v1/meta")
public class MetaController {

    /**
     * 获取推荐选项元数据（无需登录）
     * GET /api/v1/meta/recommend-options
     */
    @GetMapping("/recommend-options")
    public ApiResponse<RecommendOptionsResponse> getRecommendOptions() {
        return ApiResponse.success(RecommendOptionsResponse.builder()
                .mealTypes(buildMealTypes())
                .priceLevels(buildPriceLevels())
                .tastes(buildTastes())
                .typeTags(buildTypeTags())
                .cuisineTags(buildCuisineTags())
                .build());
    }

    private List<RecommendOptionsResponse.OptionItem> buildMealTypes() {
        List<RecommendOptionsResponse.OptionItem> items = new ArrayList<>();
        int i = 1;
        for (String value : RecommendDict.MEAL_TYPES) {
            items.add(RecommendOptionsResponse.OptionItem.builder()
                    .value(value).label(value).sortOrder(i++).build());
        }
        return items;
    }

    private List<RecommendOptionsResponse.OptionItem> buildPriceLevels() {
        List<RecommendOptionsResponse.OptionItem> items = new ArrayList<>();
        int i = 1;
        for (String value : RecommendDict.PRICE_LEVELS) {
            String label = switch (value) {
                case "15以内" -> "15元内";
                case "15-25" -> "15-25元";
                case "25-40" -> "25-40元";
                case "40以上" -> "40元以上";
                default -> value;
            };
            items.add(RecommendOptionsResponse.OptionItem.builder()
                    .value(value).label(label).hint("参考价位").sortOrder(i++).build());
        }
        return items;
    }

    private List<RecommendOptionsResponse.OptionItem> buildTastes() {
        List<RecommendOptionsResponse.OptionItem> items = new ArrayList<>();
        int i = 1;
        for (String value : RecommendDict.TASTES) {
            String hint = "辣".equals(value) ? "含麻辣" : null;
            items.add(RecommendOptionsResponse.OptionItem.builder()
                    .value(value).label(value).hint(hint).sortOrder(i++).build());
        }
        return items;
    }

    private List<RecommendOptionsResponse.OptionItem> buildTypeTags() {
        List<RecommendOptionsResponse.OptionItem> items = new ArrayList<>();
        int i = 1;
        for (String value : RecommendDict.TYPE_TAGS) {
            items.add(RecommendOptionsResponse.OptionItem.builder()
                    .value(value).label(value).sortOrder(i++).build());
        }
        return items;
    }

    private List<RecommendOptionsResponse.OptionItem> buildCuisineTags() {
        List<RecommendOptionsResponse.OptionItem> items = new ArrayList<>();
        int i = 1;
        for (String value : RecommendDict.CUISINE_TAGS) {
            items.add(RecommendOptionsResponse.OptionItem.builder()
                    .value(value).label(value).sortOrder(i++).build());
        }
        return items;
    }
}
