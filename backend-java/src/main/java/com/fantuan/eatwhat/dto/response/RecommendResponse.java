package com.fantuan.eatwhat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推荐响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendResponse {

    /**
     * 推荐的菜品
     */
    private FoodResponse food;

    /**
     * 推荐得分
     */
    private Integer score;

    /**
     * 推荐理由列表
     */
    private List<String> reasons;
}
