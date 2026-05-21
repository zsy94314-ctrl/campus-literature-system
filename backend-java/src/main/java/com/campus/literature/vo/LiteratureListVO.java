package com.campus.literature.vo;

import lombok.Data;

/**
 * 文献列表 VO
 */
@Data
public class LiteratureListVO {

    private Long id;
    private String title;
    private String authors;
    private String keywords;
    private String journal;
    private Integer publishYear;
    private Integer citationCount;
    private String doi;
    private Long categoryId;
    private String categoryName;
}
