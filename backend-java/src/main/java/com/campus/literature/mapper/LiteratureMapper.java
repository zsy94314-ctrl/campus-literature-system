package com.campus.literature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.literature.entity.Literature;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文献 Mapper
 */
@Mapper
public interface LiteratureMapper extends BaseMapper<Literature> {

    /**
     * 根据关键词、作者等条件搜索文献
     */
    List<Literature> searchLiteratures(@Param("keyword") String keyword,
                                       @Param("author") String author,
                                       @Param("categoryId") Long categoryId,
                                       @Param("year") Integer year,
                                       @Param("sortBy") String sortBy);
}
