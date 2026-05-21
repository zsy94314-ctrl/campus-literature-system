package com.campus.literature.controller;

import com.campus.literature.common.Result;
import com.campus.literature.service.AiService;
import com.campus.literature.vo.SemanticSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能检索接口
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/semantic-search")
    public Result<List<SemanticSearchVO>> semanticSearch(@RequestBody Map<String, Object> request) {
        String query = request.getOrDefault("query", "").toString();
        Integer topK = request.get("topK") != null ? Integer.valueOf(request.get("topK").toString()) : 10;
        return Result.success(aiService.semanticSearch(query, topK));
    }

    @GetMapping("/recommend/{literatureId}")
    public Result<List<SemanticSearchVO>> recommend(@PathVariable Long literatureId) {
        return Result.success(aiService.recommend(literatureId));
    }
}
