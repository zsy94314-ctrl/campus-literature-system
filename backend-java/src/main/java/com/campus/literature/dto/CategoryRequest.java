package com.campus.literature.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类请求
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Long parentId = 0L;
    private Integer sortOrder = 0;
}
