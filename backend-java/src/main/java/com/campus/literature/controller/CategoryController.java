package com.campus.literature.controller;

import com.campus.literature.common.ErrorCode;
import com.campus.literature.common.Result;
import com.campus.literature.dto.CategoryRequest;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.CategoryService;
import com.campus.literature.vo.CategoryStatisticsVO;
import com.campus.literature.vo.CategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类接口
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryVO>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @GetMapping("/statistics")
    public Result<List<CategoryStatisticsVO>> getCategoryStatistics() {
        return Result.success(categoryService.getCategoryStatistics());
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategoryRequest request) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        categoryService.create(request);
        return Result.success("新增成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        categoryService.update(id, request);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        categoryService.delete(id);
        return Result.success("删除成功", null);
    }
}
