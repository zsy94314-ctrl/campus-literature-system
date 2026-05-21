package com.campus.literature.controller;

import com.campus.literature.common.ErrorCode;
import com.campus.literature.common.PageResult;
import com.campus.literature.common.Result;
import com.campus.literature.dto.LiteratureCreateRequest;
import com.campus.literature.dto.LiteratureSearchRequest;
import com.campus.literature.dto.LiteratureUpdateRequest;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.LiteratureService;
import com.campus.literature.service.SearchHistoryService;
import com.campus.literature.vo.LiteratureDetailVO;
import com.campus.literature.vo.LiteratureListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文献接口
 */
@RestController
@RequestMapping("/api/literatures")
@RequiredArgsConstructor
public class LiteratureController {

    private final LiteratureService literatureService;
    private final SearchHistoryService searchHistoryService;

    @GetMapping("/search")
    public Result<PageResult<LiteratureListVO>> search(LiteratureSearchRequest request) {
        PageResult<LiteratureListVO> result = literatureService.search(request);
        // 记录搜索历史（仅登录用户）
        if (UserContext.getCurrentUserId() != null) {
            int count = result.getRecords() != null ? result.getRecords().size() : 0;
            searchHistoryService.saveSearchHistory(request.getKeyword(), "NORMAL", count);
        }
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<LiteratureDetailVO> getById(@PathVariable Long id) {
        return Result.success(literatureService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody LiteratureCreateRequest request) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        literatureService.create(request);
        return Result.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody LiteratureUpdateRequest request) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        literatureService.update(id, request);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        literatureService.delete(id);
        return Result.success("删除成功", null);
    }
}
