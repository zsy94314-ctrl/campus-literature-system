package com.campus.literature.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏 VO
 */
@Data
public class FavoriteVO {

    private Long id;
    private Long literatureId;
    private String title;
    private String authors;
    private LocalDateTime createTime;
}
