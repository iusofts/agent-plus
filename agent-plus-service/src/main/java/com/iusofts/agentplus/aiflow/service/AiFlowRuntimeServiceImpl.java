package com.iusofts.agentplus.aiflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.aiflow.entity.AiFlow;
import com.iusofts.agentplus.aiflow.entity.AiFlowRuntime;
import com.iusofts.agentplus.aiflow.entity.AiFlowVersion;
import com.iusofts.agentplus.aiflow.enums.RunStatusEnum;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowRuntimeService;
import com.iusofts.agentplus.aiflow.mapper.AiFlowMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowRuntimeMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowVersionMapper;
import com.iusofts.agentplus.aiflow.vo.*;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 流程运行实例 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Service
public class AiFlowRuntimeServiceImpl extends ServiceImpl<AiFlowRuntimeMapper, AiFlowRuntime> implements IAiFlowRuntimeService {

    @Resource
    private AiFlowMapper aiFlowMapper;

    @Resource
    private AiFlowVersionMapper aiFlowVersionMapper;

    @Override
    public void add(AiFlowRuntimeAddReqVo reqVo) {
        AiFlow aiFlow = aiFlowMapper.selectById(reqVo.getFlowId());
        if (aiFlow == null) {
            throw new SystemBusinessException("流程不存在");
        }

        AiFlowRuntime runtime = ModelMapperUtil.strictMap(reqVo, AiFlowRuntime.class);
        runtime.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        runtime.setRunStatus(RunStatusEnum.WAITING.getCode());
        runtime.setCreateBy(reqVo.getOperatorId());
        save(runtime);
    }

    @Override
    public PageResult<AiFlowRuntimeVo> queryPage(AiFlowRuntimeQueryPageReqVo reqVo) {
        PageResult<AiFlowRuntimeVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiFlowRuntime> wrapper = Wrappers.lambdaQuery();

        if (reqVo.getFlowId() != null) {
            wrapper.eq(AiFlowRuntime::getFlowId, reqVo.getFlowId());
        }
        if (reqVo.getRunStatus() != null) {
            wrapper.eq(AiFlowRuntime::getRunStatus, reqVo.getRunStatus());
        }

        wrapper.orderByDesc(AiFlowRuntime::getId);
        Page<AiFlowRuntime> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        IPage<AiFlowRuntime> page = page(pageParam, wrapper);

        List<AiFlowRuntimeVo> voList = page.getRecords().stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiFlowRuntimeVo.class))
                .toList();

        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public List<AiFlowRuntimeVo> queryByFlowId(Long flowId) {
        LambdaQueryWrapper<AiFlowRuntime> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiFlowRuntime::getFlowId, flowId);
        wrapper.orderByDesc(AiFlowRuntime::getId);
        List<AiFlowRuntime> list = list(wrapper);
        return list.stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiFlowRuntimeVo.class))
                .toList();
    }

    @Override
    public void remove(IdReqVo reqVo) {
        AiFlowRuntime runtime = getById(reqVo.getId());
        if (runtime == null) {
            throw new SystemBusinessException("运行实例不存在");
        }
        // 只有终态才能删除
        if (!RunStatusEnum.SUCCESS.getCode().equals(runtime.getRunStatus())
                && !RunStatusEnum.FAILED.getCode().equals(runtime.getRunStatus())
                && !RunStatusEnum.TERMINATED.getCode().equals(runtime.getRunStatus())) {
            throw new SystemBusinessException("只能删除已完成、失败或终止的运行实例");
        }
        removeById(reqVo.getId());
    }

    @Override
    public AiFlowRuntimeDetailVo getById(IdReqVo reqVo) {
        AiFlowRuntime runtime = getById(reqVo.getId());
        if (runtime == null) {
            throw new SystemBusinessException("运行实例不存在");
        }
        return ModelMapperUtil.strictMap(runtime, AiFlowRuntimeDetailVo.class);
    }

    @Override
    public void terminate(AiFlowRuntimeTerminateReqVo reqVo) {
        AiFlowRuntime runtime = getById(reqVo.getId());
        if (runtime == null) {
            throw new SystemBusinessException("运行实例不存在");
        }
        // 只有等待或运行中才能终止
        if (!RunStatusEnum.WAITING.getCode().equals(runtime.getRunStatus())
                && !RunStatusEnum.RUNNING.getCode().equals(runtime.getRunStatus())) {
            throw new SystemBusinessException("只能终止等待中或运行中的实例");
        }
        runtime.setRunStatus(RunStatusEnum.TERMINATED.getCode());
        runtime.setUpdateBy(reqVo.getOperatorId());
        updateById(runtime);
    }

    @Override
    public List<AiFlowRuntimeTraceListVo> queryTraceList(AiFlowRuntimeTraceListReqVo reqVo) {
        // 版本ID -> flowId + versionNo
        AiFlowVersion version = aiFlowVersionMapper.selectById(reqVo.getVersionId());
        if (version == null) {
            throw new SystemBusinessException("流程版本不存在");
        }

        LambdaQueryWrapper<AiFlowRuntime> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiFlowRuntime::getFlowId, version.getFlowId());
        wrapper.eq(AiFlowRuntime::getVersionNo, version.getVersionNo());
        wrapper.in(AiFlowRuntime::getTrialFlag, 1, 2);

        // 状态:指定成功/失败则精确匹配,否则默认只取成功和失败
        Integer status = reqVo.getStatus();
        if (RunStatusEnum.SUCCESS.getCode().equals(status)
                || RunStatusEnum.FAILED.getCode().equals(status)) {
            wrapper.eq(AiFlowRuntime::getRunStatus, status);
        } else {
            wrapper.in(AiFlowRuntime::getRunStatus,
                    RunStatusEnum.SUCCESS.getCode(), RunStatusEnum.FAILED.getCode());
        }

        // 日期范围(按开始时间)
        if (reqVo.getStartDate() != null) {
            wrapper.ge(AiFlowRuntime::getStartTime, reqVo.getStartDate().atStartOfDay());
        }
        if (reqVo.getEndDate() != null) {
            wrapper.lt(AiFlowRuntime::getStartTime, reqVo.getEndDate().plusDays(1).atStartOfDay());
        }

        wrapper.orderByDesc(AiFlowRuntime::getStartTime);

        List<AiFlowRuntime> list = list(wrapper);
        return list.stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiFlowRuntimeTraceListVo.class))
                .toList();
    }

}
