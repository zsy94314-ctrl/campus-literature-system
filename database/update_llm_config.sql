-- LLM API 配置表
-- 用于存储在线综述生成使用的大模型 API 配置
-- 注意：api_key 以明文存储，仅用于课程项目演示；生产环境应使用加密存储（如 AES + 环境变量）

CREATE TABLE IF NOT EXISTS llm_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    name VARCHAR(100) NOT NULL COMMENT '配置名称，如 OpenAI、DeepSeek、通义千问',
    provider VARCHAR(50) NOT NULL COMMENT 'openai-compatible / openai / deepseek / qwen / custom',
    base_url VARCHAR(255) NOT NULL COMMENT 'API Base URL',
    model VARCHAR(100) NOT NULL COMMENT '模型名称',
    api_key TEXT COMMENT 'API Key，后端保存，前端返回时必须脱敏',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    active TINYINT DEFAULT 0 COMMENT '是否为当前使用配置',
    timeout_seconds INT DEFAULT 30 COMMENT '超时时间',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='LLM API 配置表';

-- 为 review_record 表增加生成模式字段
ALTER TABLE review_record ADD COLUMN IF NOT EXISTS generation_mode VARCHAR(30) DEFAULT 'rule' COMMENT '生成模式：rule / llm / llm_fallback_rule';
