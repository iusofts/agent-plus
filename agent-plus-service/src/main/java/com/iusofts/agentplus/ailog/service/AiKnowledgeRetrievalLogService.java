package com.iusofts.agentplus.ailog.service;

import com.iusofts.agentplus.ailog.entity.AiKnowledgeRetrievalLog;
import com.iusofts.agentplus.ailog.mapper.AiKnowledgeRetrievalLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 知识库检索日志服务。
 *
 * @author Ivan
 */
@Service
public class AiKnowledgeRetrievalLogService extends ServiceImpl<AiKnowledgeRetrievalLogMapper, AiKnowledgeRetrievalLog> {

    public void saveLog(AiKnowledgeRetrievalLog log) {
        super.save(log);
    }
}
