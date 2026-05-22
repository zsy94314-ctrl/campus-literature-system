package com.campus.literature.service.impl;

import com.campus.literature.common.ErrorCode;
import com.campus.literature.dto.CategoryRequest;
import com.campus.literature.entity.Category;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.mapper.CategoryMapper;
import com.campus.literature.mapper.LiteratureMapper;
import com.campus.literature.service.CategoryService;
import com.campus.literature.vo.CategoryStatisticsVO;
import com.campus.literature.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final LiteratureMapper literatureMapper;

    @Override
    public List<CategoryVO> getAllCategories() {
        List<Category> list = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder)
        );
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<CategoryStatisticsVO> getCategoryStatistics() {
        // 1. 查询所有二级分类（parent_id != 0）
        List<Category> categories = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                        .ne(Category::getParentId, 0L)
                        .orderByAsc(Category::getSortOrder)
        );
        if (categories.isEmpty()) {
            return List.of();
        }

        // 2. 统计每个分类下的文献数量
        // 采用遍历统计方式，避免引入自定义 XML
        List<com.campus.literature.entity.Literature> allLiteratures = literatureMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.campus.literature.entity.Literature>()
        );
        Map<Long, Long> countMap = allLiteratures.stream()
                .filter(lit -> lit.getCategoryId() != null)
                .collect(Collectors.groupingBy(
                        com.campus.literature.entity.Literature::getCategoryId,
                        Collectors.counting()
                ));

        // 3. 组装结果
        return categories.stream().map(cat -> {
            CategoryStatisticsVO vo = new CategoryStatisticsVO();
            vo.setCategoryId(cat.getId());
            vo.setCategoryName(cat.getName());
            vo.setParentId(cat.getParentId());
            vo.setCount(countMap.getOrDefault(cat.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        categoryMapper.insert(category);
    }

    @Override
    public void update(Long id, CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        category.setName(request.getName());
        category.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        categoryMapper.deleteById(id);
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setParentId(category.getParentId());
        vo.setSortOrder(category.getSortOrder());
        vo.setCreateTime(category.getCreateTime());
        return vo;
    }
}
