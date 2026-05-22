package com.campus.literature.dto;

import lombok.Data;

/**
 * 相似文献推荐请求（发给 backend-ai）
 */
@Data
public class AiRecommendRequest {

    private Long literatureId;
    private Integer topK = 5;
}
