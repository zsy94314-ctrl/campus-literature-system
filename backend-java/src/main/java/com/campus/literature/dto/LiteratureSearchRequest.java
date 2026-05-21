package com.campus.literature.dto;

import lombok.Data;

/**
 * 文献检索请求
 */
@Data
public class LiteratureSearchRequest {

    private String keyword;
    private String author;
    private Long categoryId;
    private Integer year;
    private String sortBy = "relevance";
    private Integer page = 1;
    private Integer size = 10;
}
