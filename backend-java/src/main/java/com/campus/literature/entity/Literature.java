package com.campus.literature.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文献实体
 */
@Data
@TableName("literature")
public class Literature {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String authors;
    private String abstractText;
    private String keywords;
    private String journal;
    private Integer publishYear;
    private String doi;
    private Long categoryId;
    private Integer citationCount;
    private String fileUrl;
    private String source;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
