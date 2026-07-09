package com.iusofts.agentplus.ailog.service;

import com.iusofts.agentplus.ailog.entity.AiLlmCallLog;
import com.iusofts.agentplus.ailog.mapper.AiLlmCallLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * LLM 调用日志服务。
 *
 * @author Ivan
 */
@Service
public class AiLlmCallLogService extends ServiceImpl<AiLlmCallLogMapper, AiLlmCallLog> {

    public void saveLog(AiLlmCallLog log) {
        getBaseMapper().insert(log);
    }
}
