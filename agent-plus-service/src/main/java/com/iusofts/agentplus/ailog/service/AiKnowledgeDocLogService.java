package com.iusofts.agentplus.ailog.service;

import com.iusofts.agentplus.ailog.entity.AiKnowledgeDocLog;
import com.iusofts.agentplus.ailog.mapper.AiKnowledgeDocLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 知识库文档处理日志服务。
 *
 * @author Ivan
 */
@Service
public class AiKnowledgeDocLogService extends ServiceImpl<AiKnowledgeDocLogMapper, AiKnowledgeDocLog> {

    public void saveLog(AiKnowledgeDocLog log) {
        getBaseMapper().insert(log);
    }
}
