package com.campus.literature.service.impl;

import com.campus.literature.common.ErrorCode;
import com.campus.literature.dto.UserStatusRequest;
import com.campus.literature.entity.AdminLog;
import com.campus.literature.entity.User;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.mapper.AdminLogMapper;
import com.campus.literature.mapper.ReviewRecordMapper;
import com.campus.literature.mapper.SearchHistoryMapper;
import com.campus.literature.mapper.UserMapper;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.AdminService;
import com.campus.literature.vo.StatisticsVO;
import com.campus.literature.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final com.campus.literature.mapper.LiteratureMapper literatureMapper;
    private final com.campus.literature.mapper.CategoryMapper categoryMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public List<UserVO> getUserList() {
        List<User> users = userMapper.selectList(null);
        return users.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void updateUserStatus(Long id, UserStatusRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        user.setStatus(request.getStatus());
        userMapper.updateById(user);

        // 记录管理员日志
        AdminLog log = new AdminLog();
        log.setAdminId(UserContext.getCurrentUserId());
        log.setOperation("UPDATE_USER_STATUS");
        log.setTargetType("USER");
        log.setTargetId(id);
        log.setDetail("修改用户状态为: " + request.getStatus());
        adminLogMapper.insert(log);
    }

    @Override
    public StatisticsVO getStatistics() {
        StatisticsVO vo = new StatisticsVO();
        vo.setUserCount((long) userMapper.selectCount(null));
        vo.setLiteratureCount((long) literatureMapper.selectCount(null));
        vo.setReviewCount((long) reviewRecordMapper.selectCount(null));
        vo.setCategoryCount((long) categoryMapper.selectCount(null));
        return vo;
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setEmail(user.getEmail());
        return vo;
    }
}
