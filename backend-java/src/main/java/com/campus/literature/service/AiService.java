package com.campus.literature.service;

import com.campus.literature.vo.SemanticSearchVO;

import java.util.List;

/**
 * AI 服务接口
 */
public interface AiService {

    List<SemanticSearchVO> semanticSearch(String query, Integer topK);

    List<SemanticSearchVO> recommend(Long literatureId);
}
