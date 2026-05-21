package com.campus.literature.vo;

import lombok.Data;

/**
 * 用户 VO
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private Integer status;
    private String email;
}
