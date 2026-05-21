package com.campus.literature.controller;

import com.campus.literature.common.Result;
import com.campus.literature.service.FavoriteService;
import com.campus.literature.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏接口
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{literatureId}")
    public Result<Void> addFavorite(@PathVariable Long literatureId) {
        favoriteService.addFavorite(literatureId);
        return Result.success("收藏成功", null);
    }

    @DeleteMapping("/{literatureId}")
    public Result<Void> removeFavorite(@PathVariable Long literatureId) {
        favoriteService.removeFavorite(literatureId);
        return Result.success("取消收藏成功", null);
    }

    @GetMapping
    public Result<List<FavoriteVO>> getMyFavorites() {
        return Result.success(favoriteService.getMyFavorites());
    }
}
