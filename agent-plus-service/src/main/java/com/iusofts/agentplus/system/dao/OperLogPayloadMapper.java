package com.iusofts.agentplus.system.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iusofts.agentplus.system.entity.OperLogPayload;

/**
 * <p>
 * 操作日志大字段载荷 Mapper 接口
 * </p>
 *
 * @author Ivan
 * @since 2026-08-07
 */
@DS("sys")
public interface OperLogPayloadMapper extends BaseMapper<OperLogPayload> {

}
