package com.campus.literature.service;

import com.campus.literature.vo.SearchHistoryVO;

import java.util.List;

/**
 * 检索历史服务接口
 */
public interface SearchHistoryService {

    List<SearchHistoryVO> getMySearchHistory();

    void saveSearchHistory(String keyword, String searchType, Integer resultCount);
}
