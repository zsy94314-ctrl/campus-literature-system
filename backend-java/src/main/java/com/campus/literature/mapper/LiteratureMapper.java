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

    /**
     * 根据扩展词从 MySQL 补充召回文献（匹配 title/keywords/abstract_text/content）
     */
    @Select("<script>SELECT * FROM literature WHERE " +
            "<foreach collection='words' item='word' separator='OR'>" +
            "title LIKE CONCAT('%',#{word},'%') OR keywords LIKE CONCAT('%',#{word},'%') " +
            "OR abstract_text LIKE CONCAT('%',#{word},'%') OR content LIKE CONCAT('%',#{word},'%') " +
            "</foreach> LIMIT #{limit}</script>")
    List<Literature> searchByExpansionWords(@Param("words") List<String> words, @Param("limit") int limit);
}
