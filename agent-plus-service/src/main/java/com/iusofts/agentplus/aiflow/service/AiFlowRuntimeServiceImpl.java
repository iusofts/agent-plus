package com.iusofts.agentplus.aiflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowRuntimeService;
import com.iusofts.agentplus.aiflow.entity.AiFlow;
import com.iusofts.agentplus.aiflow.entity.AiFlowRuntime;
import com.iusofts.agentplus.aiflow.entity.AiFlowRuntimeNode;
import com.iusofts.agentplus.aiflow.enums.RunStatusEnum;
import com.iusofts.agentplus.aiflow.mapper.AiFlowMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowRuntimeMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowRuntimeNodeMapper;
import com.iusofts.agentplus.aiflow.vo.*;
import com.iusofts.agentplus.ailog.entity.AiKnowledgeRetrievalLog;
import com.iusofts.agentplus.ailog.entity.AiLlmCallLog;
import com.iusofts.agentplus.ailog.mapper.AiKnowledgeRetrievalLogMapper;
import com.iusofts.agentplus.ailog.mapper.AiLlmCallLogMapper;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.common.vo.IdReqVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private AiFlowRuntimeNodeMapper aiFlowRuntimeNodeMapper;

    @Resource
    private AiKnowledgeRetrievalLogMapper aiKnowledgeRetrievalLogMapper;

    @Resource
    private AiLlmCallLogMapper aiLlmCallLogMapper;

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
    public AiFlowTraceVo queryTrace(AiFlowRuntimeTraceReqVo reqVo) {
        String traceId = reqVo.getTraceId();

        // 运行实例
        AiFlowRuntime runtime = getOne(Wrappers.<AiFlowRuntime>lambdaQuery()
                .eq(AiFlowRuntime::getTraceId, traceId)
                .last("LIMIT 1"));
        if (runtime == null) {
            throw new SystemBusinessException("运行实例不存在");
        }

        AiFlowTraceVo traceVo = new AiFlowTraceVo();
        List<AiFlowTraceEventVo> events = traceVo.getTraceEvents();

        // 1. 工作流根事件
        long rootTs = toMicros(runtime.getStartTime());
        long rootDur = runtime.getCostMs() == null ? 0L : runtime.getCostMs() * 1000L;
        events.add(buildEvent(runtime.getFlowName() == null ? "工作流运行" : runtime.getFlowName(),
                rootTs, rootDur, "workflow"));

        // 2. 节点事件
        List<AiFlowRuntimeNode> nodes = aiFlowRuntimeNodeMapper.selectList(
                Wrappers.<AiFlowRuntimeNode>lambdaQuery()
                        .eq(AiFlowRuntimeNode::getRuntimeId, runtime.getId())
                        .orderByAsc(AiFlowRuntimeNode::getStartTime)
                        .orderByAsc(AiFlowRuntimeNode::getId));
        for (AiFlowRuntimeNode node : nodes) {
            if (node.getStartTime() == null) {
                continue;
            }
            long dur = node.getCostMs() == null ? 0L : node.getCostMs() * 1000L;
            events.add(buildEvent(node.getNodeName() == null ? node.getNodeId() : node.getNodeName(),
                    toMicros(node.getStartTime()), dur, "workflow.node"));
        }

        // 3. 知识库检索事件
        List<AiKnowledgeRetrievalLog> knowledgeLogs = aiKnowledgeRetrievalLogMapper.selectList(
                Wrappers.<AiKnowledgeRetrievalLog>lambdaQuery()
                        .eq(AiKnowledgeRetrievalLog::getTraceId, traceId)
                        .orderByAsc(AiKnowledgeRetrievalLog::getStartTime));
        for (AiKnowledgeRetrievalLog log : knowledgeLogs) {
            if (log.getStartTime() == null) {
                continue;
            }
            long dur = log.getDuration() == null ? 0L : log.getDuration() * 1000L;
            String name = log.getKnowledgeBaseName() == null ? "知识检索" : log.getKnowledgeBaseName();
            events.add(buildEvent(name, toMicros(log.getStartTime()), dur, "knowledge"));
        }

        // 4. 大模型调用事件
        List<AiLlmCallLog> llmLogs = aiLlmCallLogMapper.selectList(
                Wrappers.<AiLlmCallLog>lambdaQuery()
                        .eq(AiLlmCallLog::getTraceId, traceId)
                        .orderByAsc(AiLlmCallLog::getStartTime));
        for (AiLlmCallLog log : llmLogs) {
            if (log.getStartTime() == null) {
                continue;
            }
            long dur = log.getDuration() == null ? 0L : log.getDuration() * 1000L;
            String name = log.getModelName() == null ? "LLM推理" : log.getModelName();
            events.add(buildEvent(name, toMicros(log.getStartTime()), dur, "llm"));
        }

        // 按开始时间升序、时长降序排序(父区间在前),便于前端火焰图渲染
        events.sort((a, b) -> {
            int c = Long.compare(a.getTs(), b.getTs());
            return c != 0 ? c : Long.compare(b.getDur(), a.getDur());
        });

        return traceVo;
    }

    private AiFlowTraceEventVo buildEvent(String name, long ts, long dur, String cat) {
        AiFlowTraceEventVo event = new AiFlowTraceEventVo();
        event.setName(name);
        event.setPh("X");
        event.setTs(ts);
        event.setDur(dur);
        event.setCat(cat);
        return event;
    }

    /** LocalDateTime 转微秒时间戳(系统默认时区)。 */
    private long toMicros(LocalDateTime time) {
        if (time == null) {
            return 0L;
        }
        var instant = time.atZone(ZoneId.systemDefault()).toInstant();
        return instant.getEpochSecond() * 1_000_000L + instant.getNano() / 1000L;
    }

}
