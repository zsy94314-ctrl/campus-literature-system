package com.campus.literature.service;

import com.campus.literature.vo.AiSearchResultVO;

import java.util.List;
import java.util.Map;

/**
 * AI 服务接口
 */
public interface AiService {

    Map<String, Object> health();

    Map<String, Object> rebuildIndex();

    List<AiSearchResultVO> semanticSearch(String query, Integer topK);

    List<AiSearchResultVO> recommend(Long literatureId);
}
