package com.campus.literature.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 生成综述请求
 */
@Data
public class ReviewGenerateRequest {

    @NotBlank(message = "综述主题不能为空")
    private String topic;

    @NotEmpty(message = "文献列表不能为空")
    private List<Long> literatureIds;
}
