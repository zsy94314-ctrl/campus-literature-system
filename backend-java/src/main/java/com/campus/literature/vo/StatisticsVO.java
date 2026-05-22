package com.campus.literature.vo;

import lombok.Data;

import java.util.List;

/**
 * 系统统计 VO
 */
@Data
public class StatisticsVO {

    // 基础计数
    private Long userCount;
    private Long literatureCount;
    private Long reviewCount;
    private Long categoryCount;

    // 扩展计数
    private Long favoriteCount;
    private Long searchHistoryCount;
    private Long llmConfigCount;

    // 当前活跃 LLM 配置
    private String activeLlmName;
    private String activeLlmProvider;
    private String activeLlmModel;
    private Boolean activeLlmEnabled;

    // 分类 Top 10
    private List<CategoryStatVO> categoryTop;

    // 年份分布
    private List<YearDistributionVO> yearDistribution;

    // 文献类型分布
    private List<DocumentTypeDistributionVO> documentTypeDistribution;

    // 综述生成方式分布
    private List<ReviewModeDistributionVO> reviewModeDistribution;

    // 检索类型分布
    private List<SearchTypeDistributionVO> searchTypeDistribution;
}
