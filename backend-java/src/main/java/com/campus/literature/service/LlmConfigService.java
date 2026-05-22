package com.campus.literature.service;

import com.campus.literature.dto.LlmConfigCreateRequest;
import com.campus.literature.dto.LlmConfigUpdateRequest;
import com.campus.literature.entity.LlmConfig;
import com.campus.literature.vo.LlmConfigVO;

import java.util.List;

/**
 * LLM 配置服务
 */
public interface LlmConfigService {

    List<LlmConfigVO> listAll();

    LlmConfigVO getActive();

    LlmConfigVO create(LlmConfigCreateRequest request);

    LlmConfigVO update(Long id, LlmConfigUpdateRequest request);

    void delete(Long id);

    void activate(Long id);

    String testConnection(Long id);
}
