package com.iusofts.agentplus.system.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iusofts.agentplus.system.entity.OperLog;

/**
 * <p>
 * 操作日志记录 Mapper 接口
 * </p>
 *
 * @author Ivan
 * @since 2020-12-09
 */
@DS("sys")
public interface OperLogMapper extends BaseMapper<OperLog> {

}
