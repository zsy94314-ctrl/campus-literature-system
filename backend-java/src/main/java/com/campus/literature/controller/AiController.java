package com.campus.literature.controller;

import com.campus.literature.common.ErrorCode;
import com.campus.literature.common.Result;
import com.campus.literature.dto.AiSearchRequest;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.AiService;
import com.campus.literature.vo.AiSearchResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 智能检索接口
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.success(aiService.health());
    }

    /**
     * 重建智能索引（管理员）
     */
    @PostMapping("/rebuild-index")
    public Result<Map<String, Object>> rebuildIndex() {
        if (!UserContext.isAdmin()) {
            return Result.error(ErrorCode.FORBIDDEN.getCode(), "无权限，仅管理员可重建索引");
        }
        return Result.success("索引重建成功", aiService.rebuildIndex());
    }

    /**
     * 语义检索
     */
    @PostMapping("/semantic-search")
    public Result<List<AiSearchResultVO>> semanticSearch(@Valid @RequestBody AiSearchRequest request) {
        return Result.success(aiService.semanticSearch(request.getQuery(), request.getTopK()));
    }

    /**
     * 相似文献推荐
     */
    @GetMapping("/recommend/{literatureId}")
    public Result<List<AiSearchResultVO>> recommend(@PathVariable Long literatureId) {
        return Result.success(aiService.recommend(literatureId));
    }
}
