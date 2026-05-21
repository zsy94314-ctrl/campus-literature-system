package com.campus.literature.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类 VO
 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
