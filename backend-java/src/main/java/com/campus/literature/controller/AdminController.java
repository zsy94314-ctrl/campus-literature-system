package com.campus.literature.controller;

import com.campus.literature.common.ErrorCode;
import com.campus.literature.common.Result;
import com.campus.literature.dto.UserStatusRequest;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.AdminService;
import com.campus.literature.vo.StatisticsVO;
import com.campus.literature.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员接口
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public Result<List<UserVO>> getUserList() {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return Result.success(adminService.getUserList());
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        adminService.updateUserStatus(id, request);
        return Result.success("修改成功", null);
    }

    @GetMapping("/statistics")
    public Result<StatisticsVO> getStatistics() {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return Result.success(adminService.getStatistics());
    }
}
