package com.iusofts.agentplus.ailog.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.ailog.entity.AiTraceSpan;
import com.iusofts.agentplus.ailog.mapper.AiTraceSpanMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * AI Trace Span 落库服务。
 *
 * @author Ivan
 * @since 2026-07-24
 */
@Service
public class AiTraceSpanService extends ServiceImpl<AiTraceSpanMapper, AiTraceSpan> {

    /**
     * 批量插入 Span 记录。
     */
    public void batchSave(Collection<AiTraceSpan> spans) {
        if (spans != null && !spans.isEmpty()) {
            saveBatch(spans);
        }
    }
}