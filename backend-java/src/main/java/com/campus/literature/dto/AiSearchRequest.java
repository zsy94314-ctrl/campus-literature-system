package com.campus.literature.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 语义检索请求
 */
@Data
public class AiSearchRequest {

    @NotBlank(message = "检索语句不能为空")
    private String query;

    private Integer topK = 10;
}
