package com.campus.literature.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM 配置 VO（前端展示用，API Key 脱敏）
 */
@Data
public class LlmConfigVO {

    private Long id;
    private String name;
    private String provider;
    private String baseUrl;
    private String model;
    private String apiKeyMasked;
    private Integer enabled;
    private Integer active;
    private Integer timeoutSeconds;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
