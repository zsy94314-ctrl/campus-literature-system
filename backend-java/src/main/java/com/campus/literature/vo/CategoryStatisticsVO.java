package com.campus.literature.vo;

import lombok.Data;

/**
 * 分类统计 VO
 */
@Data
public class CategoryStatisticsVO {

    private Long categoryId;
    private String categoryName;
    private Long parentId;
    private Long count;
}
