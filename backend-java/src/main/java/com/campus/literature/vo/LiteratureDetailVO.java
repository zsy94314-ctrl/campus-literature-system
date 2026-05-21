package com.campus.literature.vo;

import lombok.Data;

/**
 * 文献详情 VO
 */
@Data
public class LiteratureDetailVO {

    private Long id;
    private String title;
    private String authors;
    private String abstractText;
    private String keywords;
    private String journal;
    private Integer publishYear;
    private String doi;
    private Integer citationCount;
    private Long categoryId;
    private String categoryName;
    private String documentType;
    private String sourceUrl;
    private String content;
}
