package com.campus.literature.dto;

import lombok.Data;

/**
 * 文献检索请求
 */
@Data
public class LiteratureSearchRequest {

    private String keyword;
    private String title;
    private String author;
    private String journal;
    private String doi;
    private Long categoryId;
    private String documentType;
    private Integer year;
    private Integer startYear;
    private Integer endYear;
    private String sortBy = "relevance";
    private Integer page = 1;
    private Integer size = 10;
}
