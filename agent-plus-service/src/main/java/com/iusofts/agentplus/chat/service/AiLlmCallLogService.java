package com.iusofts.agentplus.chat.service;

import com.iusofts.agentplus.chat.entity.AiLlmCallLog;
import com.iusofts.agentplus.chat.mapper.AiLlmCallLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * AI大模型调用日志 服务类
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Service
public class AiLlmCallLogService extends ServiceImpl<AiLlmCallLogMapper, AiLlmCallLog> {

    public void saveLog(AiLlmCallLog log) {
        if (log != null) {
            getBaseMapper().insert(log);
        }
    }
}
