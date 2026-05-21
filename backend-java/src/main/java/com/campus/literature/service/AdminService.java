package com.campus.literature.service;

import com.campus.literature.dto.UserStatusRequest;
import com.campus.literature.vo.StatisticsVO;
import com.campus.literature.vo.UserVO;

import java.util.List;

/**
 * 管理员服务接口
 */
public interface AdminService {

    List<UserVO> getUserList();

    void updateUserStatus(Long id, UserStatusRequest request);

    StatisticsVO getStatistics();
}
