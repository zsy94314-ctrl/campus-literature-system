package com.campus.literature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.literature.entity.Favorite;
import com.campus.literature.vo.FavoriteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏 Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT f.id, f.literature_id AS literatureId, l.title, l.authors, f.create_time AS createTime " +
            "FROM favorite f JOIN literature l ON f.literature_id = l.id " +
            "WHERE f.user_id = #{userId} ORDER BY f.create_time DESC")
    List<FavoriteVO> selectFavoriteList(@Param("userId") Long userId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND literature_id = #{literatureId} LIMIT 1")
    Favorite selectByUserAndLiterature(@Param("userId") Long userId, @Param("literatureId") Long literatureId);
}
