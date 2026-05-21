package com.campus.literature.vo;

import lombok.Data;

/**
 * 登录返回 VO
 */
@Data
public class LoginVO {

    private String token;
    private UserVO user;
}
