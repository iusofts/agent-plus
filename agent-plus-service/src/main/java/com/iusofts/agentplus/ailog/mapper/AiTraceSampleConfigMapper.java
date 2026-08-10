package com.iusofts.agentplus.ailog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iusofts.agentplus.ailog.entity.AiTraceSampleConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI Trace 采样率配置 Mapper。
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Mapper
public interface AiTraceSampleConfigMapper extends BaseMapper<AiTraceSampleConfig> {
}
