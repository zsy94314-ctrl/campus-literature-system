package com.campus.literature.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 综述记录实体
 */
@Data
@TableName("review_record")
public class ReviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String topic;
    private String literatureIds;
    private String content;
    private String referenceText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
