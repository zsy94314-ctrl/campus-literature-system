package com.campus.literature.service.impl;

import com.campus.literature.common.ErrorCode;
import com.campus.literature.entity.Favorite;
import com.campus.literature.exception.BusinessException;
import com.campus.literature.mapper.FavoriteMapper;
import com.campus.literature.security.UserContext;
import com.campus.literature.service.FavoriteService;
import com.campus.literature.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收藏服务实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;

    @Override
    public void addFavorite(Long literatureId) {
        Long userId = UserContext.getCurrentUserId();
        Favorite existing = favoriteMapper.selectByUserAndLiterature(userId, literatureId);
        if (existing != null) {
            throw new BusinessException(ErrorCode.ALREADY_FAVORITED);
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setLiteratureId(literatureId);
        favoriteMapper.insert(favorite);
    }

    @Override
    public void removeFavorite(Long literatureId) {
        Long userId = UserContext.getCurrentUserId();
        Favorite existing = favoriteMapper.selectByUserAndLiterature(userId, literatureId);
        if (existing == null) {
            throw new BusinessException(ErrorCode.FAVORITE_NOT_FOUND);
        }
        favoriteMapper.deleteById(existing.getId());
    }

    @Override
    public List<FavoriteVO> getMyFavorites() {
        Long userId = UserContext.getCurrentUserId();
        return favoriteMapper.selectFavoriteList(userId);
    }
}
