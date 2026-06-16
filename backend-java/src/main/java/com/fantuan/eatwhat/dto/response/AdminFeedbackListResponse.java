package com.fantuan.eatwhat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理后台 - 反馈列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFeedbackListResponse {

    /** 反馈条目列表 */
    private List<FeedbackResponse> items;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private int page;

    /** 每页条数 */
    private int size;
}
