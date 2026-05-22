package com.campus.literature.controller;

import com.campus.literature.common.ErrorCode;
import com.campus.literature.common.Result;
import com.campus.literature.dto.LlmConfigCreateRequest;
import com.campus.literature.dto.LlmConfigUpdateRequest;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.LlmConfigService;
import com.campus.literature.vo.LlmConfigVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LLM API 配置管理接口（管理员）
 */
@RestController
@RequestMapping("/api/admin/llm-configs")
@RequiredArgsConstructor
public class LlmConfigController {

    private final LlmConfigService llmConfigService;

    private void checkAdmin() {
        if (!UserContext.isAdmin()) {
            throw new com.campus.literature.exception.BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权限，仅管理员可访问");
        }
    }

    @GetMapping
    public Result<List<LlmConfigVO>> list() {
        checkAdmin();
        return Result.success(llmConfigService.listAll());
    }

    @GetMapping("/active")
    public Result<LlmConfigVO> getActive() {
        checkAdmin();
        LlmConfigVO vo = llmConfigService.getActive();
        return Result.success(vo);
    }

    @PostMapping
    public Result<LlmConfigVO> create(@Valid @RequestBody LlmConfigCreateRequest request) {
        checkAdmin();
        return Result.success("创建成功", llmConfigService.create(request));
    }

    @PutMapping("/{id}")
    public Result<LlmConfigVO> update(@PathVariable Long id, @Valid @RequestBody LlmConfigUpdateRequest request) {
        checkAdmin();
        return Result.success("更新成功", llmConfigService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkAdmin();
        llmConfigService.delete(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/activate")
    public Result<Void> activate(@PathVariable Long id) {
        checkAdmin();
        llmConfigService.activate(id);
        return Result.success("已设为当前使用配置", null);
    }

    @PostMapping("/{id}/test")
    public Result<String> test(@PathVariable Long id) {
        checkAdmin();
        String result = llmConfigService.testConnection(id);
        return Result.success("连接成功", result);
    }
}
