package com.iusofts.agentplus.ailog.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.ailog.entity.AiTraceSpanPayload;
import com.iusofts.agentplus.ailog.mapper.AiTraceSpanPayloadMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Span 载荷落库服务。
 *
 * @author Ivan
 * @since 2026-07-24
 */
@Service
public class AiTraceSpanPayloadService extends ServiceImpl<AiTraceSpanPayloadMapper, AiTraceSpanPayload> {

    /**
     * 批量插入载荷记录。
     */
    public void batchSave(Collection<AiTraceSpanPayload> payloads) {
        if (payloads != null && !payloads.isEmpty()) {
            saveBatch(payloads);
        }
    }
}