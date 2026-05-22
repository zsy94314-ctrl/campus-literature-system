package com.campus.literature.vo;

import lombok.Data;

/**
 * 文献类型分布统计 VO
 */
@Data
public class DocumentTypeDistributionVO {

    private String type;
    private Long count;
}
