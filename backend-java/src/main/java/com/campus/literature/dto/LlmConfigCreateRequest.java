package com.campus.literature.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增 LLM 配置请求
 */
@Data
public class LlmConfigCreateRequest {

    @NotBlank(message = "配置名称不能为空")
    private String name;

    @NotBlank(message = "Provider 不能为空")
    private String provider;

    @NotBlank(message = "Base URL 不能为空")
    private String baseUrl;

    @NotBlank(message = "模型名称不能为空")
    private String model;

    private String apiKey;

    @NotNull(message = "启用状态不能为空")
    private Integer enabled;

    @NotNull(message = "是否当前使用不能为空")
    private Integer active;

    @NotNull(message = "超时时间不能为空")
    private Integer timeoutSeconds;

    private String remark;
}
