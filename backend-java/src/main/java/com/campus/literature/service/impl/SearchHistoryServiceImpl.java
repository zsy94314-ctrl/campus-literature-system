package com.campus.literature.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.literature.entity.SearchHistory;
import com.campus.literature.mapper.SearchHistoryMapper;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.SearchHistoryService;
import com.campus.literature.vo.SearchHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 检索历史服务实现
 */
@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private final SearchHistoryMapper searchHistoryMapper;

    @Override
    public List<SearchHistoryVO> getMySearchHistory() {
        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getCreateTime)
                .last("LIMIT 50");
        List<SearchHistory> list = searchHistoryMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void saveSearchHistory(String keyword, String searchType, Integer resultCount) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return;
        }
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword != null ? keyword : "");
        history.setSearchType(searchType);
        history.setResultCount(resultCount);
        searchHistoryMapper.insert(history);
    }

    private SearchHistoryVO convertToVO(SearchHistory history) {
        SearchHistoryVO vo = new SearchHistoryVO();
        vo.setId(history.getId());
        vo.setKeyword(history.getKeyword());
        vo.setSearchType(history.getSearchType());
        vo.setResultCount(history.getResultCount());
        vo.setCreateTime(history.getCreateTime());
        return vo;
    }
}
