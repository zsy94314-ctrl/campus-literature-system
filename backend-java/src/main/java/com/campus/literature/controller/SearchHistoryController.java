package com.campus.literature.controller;

import com.campus.literature.common.Result;
import com.campus.literature.service.SearchHistoryService;
import com.campus.literature.vo.SearchHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 检索历史接口
 */
@RestController
@RequestMapping("/api/search-history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @GetMapping
    public Result<List<SearchHistoryVO>> getMySearchHistory() {
        return Result.success(searchHistoryService.getMySearchHistory());
    }
}
