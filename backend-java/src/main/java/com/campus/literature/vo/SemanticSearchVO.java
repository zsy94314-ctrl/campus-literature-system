package com.campus.literature.vo;

import lombok.Data;

/**
 * 语义检索结果 VO
 */
@Data
public class SemanticSearchVO {

    private Long literatureId;
    private String title;
    private Double similarity;
}
