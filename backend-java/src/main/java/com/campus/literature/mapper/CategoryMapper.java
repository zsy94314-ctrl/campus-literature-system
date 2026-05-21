package com.campus.literature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.literature.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
