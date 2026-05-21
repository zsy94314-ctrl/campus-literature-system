package com.campus.literature.vo;

import lombok.Data;

/**
 * 系统统计 VO
 */
@Data
public class StatisticsVO {

    private Long userCount;
    private Long literatureCount;
    private Long searchCount;
    private Long reviewCount;
}
