package com.campus.literature.service;

import com.campus.literature.vo.FavoriteVO;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    void addFavorite(Long literatureId);

    void removeFavorite(Long literatureId);

    List<FavoriteVO> getMyFavorites();
}
