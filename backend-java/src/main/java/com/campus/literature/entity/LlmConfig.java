package com.campus.literature.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM API 配置实体
 */
@Data
@TableName("llm_config")
public class LlmConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String provider;
    private String baseUrl;
    private String model;
    private String apiKey;
    private Integer enabled;
    private Integer active;
    private Integer timeoutSeconds;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
