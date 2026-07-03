package com.iusofts.agentplus.aiflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowRuntimeNodeService;
import com.iusofts.agentplus.aiflow.entity.AiFlowRuntimeNode;
import com.iusofts.agentplus.aiflow.mapper.AiFlowRuntimeNodeMapper;
import com.iusofts.agentplus.aiflow.vo.AiFlowRuntimeNodeDetailVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowRuntimeNodeQueryReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowRuntimeNodeVo;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.common.vo.IdReqVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 运行节点明细 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Service
public class AiFlowRuntimeNodeServiceImpl extends ServiceImpl<AiFlowRuntimeNodeMapper, AiFlowRuntimeNode> implements IAiFlowRuntimeNodeService {

    @Override
    public List<AiFlowRuntimeNodeVo> queryByRuntimeId(AiFlowRuntimeNodeQueryReqVo reqVo) {
        LambdaQueryWrapper<AiFlowRuntimeNode> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiFlowRuntimeNode::getRuntimeId, reqVo.getRuntimeId());
        wrapper.orderByAsc(AiFlowRuntimeNode::getId);
        List<AiFlowRuntimeNode> list = list(wrapper);
        return list.stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiFlowRuntimeNodeVo.class))
                .toList();
    }

    @Override
    public AiFlowRuntimeNodeDetailVo getById(IdReqVo reqVo) {
        AiFlowRuntimeNode node = getById(reqVo.getId());
        if (node == null) {
            throw new SystemBusinessException("节点不存在");
        }
        return ModelMapperUtil.strictMap(node, AiFlowRuntimeNodeDetailVo.class);
    }

}
