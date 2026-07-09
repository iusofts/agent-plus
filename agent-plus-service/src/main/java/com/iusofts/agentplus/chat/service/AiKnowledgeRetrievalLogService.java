package com.iusofts.agentplus.chat.service;

import com.iusofts.agentplus.chat.entity.AiKnowledgeRetrievalLog;
import com.iusofts.agentplus.chat.mapper.AiKnowledgeRetrievalLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * AI知识库检索日志 服务类
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Service
public class AiKnowledgeRetrievalLogService extends ServiceImpl<AiKnowledgeRetrievalLogMapper, AiKnowledgeRetrievalLog> {

    public void saveLog(AiKnowledgeRetrievalLog log) {
        if (log != null) {
            getBaseMapper().insert(log);
        }
    }
}
