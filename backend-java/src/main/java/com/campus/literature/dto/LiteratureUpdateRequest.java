package com.campus.literature.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改文献请求
 */
@Data
public class LiteratureUpdateRequest {

    @NotBlank(message = "文献标题不能为空")
    private String title;

    private String authors;
    private String abstractText;
    private String keywords;
    private String journal;
    private Integer publishYear;
    private String doi;
    private Long categoryId;
    private Integer citationCount;
    private String documentType;
    private String sourceUrl;
    private String content;
}
