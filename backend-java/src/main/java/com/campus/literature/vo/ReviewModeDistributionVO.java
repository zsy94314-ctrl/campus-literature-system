package com.campus.literature.vo;

import lombok.Data;

/**
 * 综述生成方式分布统计 VO
 */
@Data
public class ReviewModeDistributionVO {

    private String mode;
    private String label;
    private Long count;
}
