package com.campus.literature.dto;

import lombok.Data;

/**
 * 传给 backend-ai 的文献文档 DTO
 */
@Data
public class AiDocumentDTO {

    private Long id;
    private String title;
    private String categoryName;
    private String documentType;
    private String keywords;
    private String abstractText;
    private String content;
}
