package com.campus.literature.vo;

import lombok.Data;

/**
 * 检索类型分布统计 VO
 */
@Data
public class SearchTypeDistributionVO {

    private String type;
    private String label;
    private Long count;
}
