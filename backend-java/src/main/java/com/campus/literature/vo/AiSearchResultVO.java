package com.campus.literature.vo;

import lombok.Data;

/**
 * AI 语义检索 / 相似推荐结果 VO
 */
@Data
public class AiSearchResultVO {

    private Long id;
    private String title;
    private String authors;
    private String abstractText;
    private String keywords;
    private String journal;
    private Integer publishYear;
    private Long categoryId;
    private String categoryName;
    private Integer citationCount;
    private String documentType;
    private Double similarity;

    /** 综合排序分（semanticSimilarity * 0.60 + keywordScore * 0.30 + categoryScore * 0.10） */
    private Double finalScore;

    /** 命中原因说明 */
    private String matchReason;
}
