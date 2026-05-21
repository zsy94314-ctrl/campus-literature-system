package com.campus.literature.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文献向量索引实体
 */
@Data
@TableName("literature_vector")
public class LiteratureVector {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long literatureId;
    private Long vectorIndexId;
    private String embeddingModel;
    private LocalDateTime updateTime;
}
