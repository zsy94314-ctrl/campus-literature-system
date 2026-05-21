package com.campus.literature.controller;

import com.campus.literature.common.Result;
import com.campus.literature.dto.ReviewGenerateRequest;
import com.campus.literature.service.ReviewRecordService;
import com.campus.literature.vo.ReviewRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 综述生成接口
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewRecordController {

    private final ReviewRecordService reviewRecordService;

    @PostMapping("/generate")
    public Result<ReviewRecordVO> generate(@Valid @RequestBody ReviewGenerateRequest request) {
        ReviewRecordVO vo = reviewRecordService.generate(request);
        return Result.success("生成成功", vo);
    }

    @GetMapping("/history")
    public Result<List<ReviewRecordVO>> getMyHistory() {
        return Result.success(reviewRecordService.getMyHistory());
    }

    @GetMapping("/{id}")
    public Result<ReviewRecordVO> getById(@PathVariable Long id) {
        return Result.success(reviewRecordService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewRecordService.delete(id);
        return Result.success("删除成功", null);
    }
}
