package com.iusofts.agentplus.aiflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowService;
import com.iusofts.agentplus.aiflow.entity.AiFlow;
import com.iusofts.agentplus.aiflow.enums.FlowStatusEnum;
import com.iusofts.agentplus.aiflow.mapper.AiFlowMapper;
import com.iusofts.agentplus.aiflow.vo.*;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * AI流程 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Service
public class AiFlowServiceImpl extends ServiceImpl<AiFlowMapper, AiFlow> implements IAiFlowService {

    @Resource
    private IdService idService;

    @Override
    public void add(AiFlowAddReqVo reqVo) {
        // 检查编码是否已存在
        LambdaQueryWrapper<AiFlow> checkWrapper = Wrappers.lambdaQuery();
        checkWrapper.eq(AiFlow::getCode, reqVo.getCode());
        if (count(checkWrapper) > 0) {
            throw new SystemBusinessException("流程编码已存在");
        }

        AiFlow aiFlow = ModelMapperUtil.strictMap(reqVo, AiFlow.class);
        Integer uid = idService.generateUid(UidTypeEnum.FLOW);
        aiFlow.setId(uid.longValue());
        aiFlow.setCreateBy(reqVo.getOperatorId());
        aiFlow.setStatus(FlowStatusEnum.ENABLED.getCode());
        aiFlow.setLatestVersion("v1.0.0");
        aiFlow.setOnlineVersion("");
        save(aiFlow);
    }

    @Override
    public void edit(AiFlowEditReqVo reqVo) {
        AiFlow aiFlow = getById(reqVo.getId());
        if (aiFlow == null) {
            throw new SystemBusinessException("流程不存在");
        }

        AiFlow updateFlow = ModelMapperUtil.strictMap(reqVo, AiFlow.class);
        updateFlow.setUpdateBy(reqVo.getOperatorId());
        updateById(updateFlow);
    }

    @Override
    public PageResult<AiFlowVo> queryPage(AiFlowQueryPageReqVo reqVo) {
        PageResult<AiFlowVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiFlow> wrapper = Wrappers.lambdaQuery();

        if (reqVo.getType() != null) {
            wrapper.eq(AiFlow::getType, reqVo.getType());
        }
        if (StringUtils.isNotBlank(reqVo.getName())) {
            wrapper.like(AiFlow::getName, reqVo.getName());
        }
        if (reqVo.getStatus() != null) {
            wrapper.eq(AiFlow::getStatus, reqVo.getStatus());
        }

        wrapper.orderByDesc(AiFlow::getId);
        Page<AiFlow> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        IPage<AiFlow> page = page(pageParam, wrapper);

        List<AiFlowVo> voList = page.getRecords().stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiFlowVo.class))
                .toList();

        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public List<AiFlowVo> queryAll() {
        LambdaQueryWrapper<AiFlow> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiFlow::getStatus, FlowStatusEnum.ENABLED.getCode());
        wrapper.orderByDesc(AiFlow::getId);
        List<AiFlow> list = list(wrapper);
        return list.stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiFlowVo.class))
                .toList();
    }

    @Override
    public void remove(IdReqVo reqVo) {
        AiFlow aiFlow = getById(reqVo.getId());
        if (aiFlow == null) {
            throw new SystemBusinessException("流程不存在");
        }
        removeById(reqVo.getId());
    }

    @Override
    public AiFlowDetailVo getById(IdReqVo reqVo) {
        AiFlow aiFlow = getById(reqVo.getId());
        if (aiFlow == null) {
            throw new SystemBusinessException("流程不存在");
        }
        return ModelMapperUtil.strictMap(aiFlow, AiFlowDetailVo.class);
    }

    @Override
    public void setStatus(AiFlowSetStatusReqVo reqVo) {
        AiFlow aiFlow = getById(reqVo.getId());
        if (aiFlow == null) {
            throw new SystemBusinessException("流程不存在");
        }
        aiFlow.setStatus(reqVo.getStatus());
        aiFlow.setUpdateBy(reqVo.getOperatorId());
        updateById(aiFlow);
    }

}
