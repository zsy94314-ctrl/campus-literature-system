package com.campus.literature.service;

import com.campus.literature.dto.LoginRequest;
import com.campus.literature.dto.RegisterRequest;
import com.campus.literature.vo.LoginVO;

/**
 * 用户服务接口
 */
public interface UserService {

    void register(RegisterRequest request);

    LoginVO login(LoginRequest request);
}
