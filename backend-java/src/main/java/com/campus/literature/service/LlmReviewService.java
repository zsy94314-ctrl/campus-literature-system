package com.campus.literature.service;

import com.campus.literature.entity.Literature;
import com.campus.literature.entity.LlmConfig;

import java.util.List;

/**
 * LLM 综述生成服务
 */
public interface LlmReviewService {

    /**
     * 使用 LLM 生成综述
     *
     * @param topic        综述主题
     * @param literatures  选中的文献列表
     * @param config       LLM 配置
     * @return 生成的综述文本
     * @throws Exception 生成失败时抛出异常，由上层降级处理
     */
    String generateReview(String topic, List<Literature> literatures, LlmConfig config) throws Exception;
}
