package com.campus.literature.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检索历史 VO
 */
@Data
public class SearchHistoryVO {

    private Long id;
    private String keyword;
    private String searchType;
    private Integer resultCount;
    private LocalDateTime createTime;
}
