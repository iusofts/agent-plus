package com.iusofts.agentplus.chat.service;

import com.iusofts.agentplus.chat.entity.AiKnowledgeDocLog;
import com.iusofts.agentplus.chat.mapper.AiKnowledgeDocLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * AI知识库文档处理日志 服务类
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Service
public class AiKnowledgeDocLogService extends ServiceImpl<AiKnowledgeDocLogMapper, AiKnowledgeDocLog> {

    public void saveLog(AiKnowledgeDocLog log) {
        if (log != null) {
            getBaseMapper().insert(log);
        }
    }
}
