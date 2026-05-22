package com.campus.literature.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 综述记录 VO
 */
@Data
public class ReviewRecordVO {

    private Long id;
    private String topic;
    private String content;
    private String generationMode;
    private List<ReferenceVO> references;
    private LocalDateTime createTime;

    @Data
    public static class ReferenceVO {
        private Long literatureId;
        private String title;
    }
}
