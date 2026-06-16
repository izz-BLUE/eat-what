package com.fantuan.eatwhat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 意见反馈请求 DTO
 */
@Data
public class FeedbackRequest {

    /** 反馈类型：FEATURE/BUG/RECOMMENDATION/UI/OTHER */
    @NotBlank(message = "反馈类型不能为空")
    @Pattern(regexp = "^(FEATURE|BUG|RECOMMENDATION|UI|OTHER)$", message = "反馈类型不正确")
    private String type;

    /** 满意度评分（1-5，可选） */
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer rating;

    /** 反馈内容（5-500字） */
    @NotBlank(message = "反馈内容不能为空")
    @Size(min = 5, max = 500, message = "反馈内容长度需在5-500字之间")
    private String content;

    /** 联系方式（可选，最长100字） */
    @Size(max = 100, message = "联系方式最长100字")
    private String contact;

    /** 来源页面路径（可选，最长128字） */
    @Size(max = 128, message = "来源页面路径最长128字")
    private String page;

    /** 微信环境信息（可选，最长1000字） */
    @Size(max = 1000, message = "环境信息最长1000字")
    private String systemInfo;
}
