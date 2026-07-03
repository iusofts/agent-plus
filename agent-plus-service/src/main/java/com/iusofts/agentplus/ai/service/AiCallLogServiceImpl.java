package com.iusofts.agentplus.ai.service;

import com.iusofts.agentplus.ai.interfaces.IAiCallLogService;
import com.iusofts.agentplus.ai.entity.AiCallLog;
import com.iusofts.agentplus.ai.mapper.AiCallLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * ai对话消息 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-05-08
 */
@Service
public class AiCallLogServiceImpl extends ServiceImpl<AiCallLogMapper, AiCallLog> implements IAiCallLogService {

}
