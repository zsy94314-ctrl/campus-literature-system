package com.campus.literature.service;

import com.campus.literature.dto.CategoryRequest;
import com.campus.literature.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    List<CategoryVO> getAllCategories();

    void create(CategoryRequest request);

    void update(Long id, CategoryRequest request);

    void delete(Long id);
}
