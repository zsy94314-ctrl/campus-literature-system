package com.campus.literature.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.literature.entity.AdminLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员日志 Mapper
 */
@Mapper
public interface AdminLogMapper extends BaseMapper<AdminLog> {
}
