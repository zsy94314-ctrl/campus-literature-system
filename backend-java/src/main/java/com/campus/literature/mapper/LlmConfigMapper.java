package com.campus.literature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.literature.entity.LlmConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * LLM 配置 Mapper
 */
@Mapper
public interface LlmConfigMapper extends BaseMapper<LlmConfig> {

    @Select("SELECT * FROM llm_config WHERE active = 1 AND enabled = 1 LIMIT 1")
    LlmConfig selectActive();

    @Update("UPDATE llm_config SET active = 0 WHERE active = 1")
    int deactivateAll();

    @Select("SELECT api_key FROM llm_config WHERE id = #{id}")
    String selectApiKeyById(@Param("id") Long id);
}
