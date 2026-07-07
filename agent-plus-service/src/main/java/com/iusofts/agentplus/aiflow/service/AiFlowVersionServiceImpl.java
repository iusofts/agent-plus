package com.iusofts.agentplus.aiflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowVersionService;
import com.iusofts.agentplus.aiflow.entity.AiFlow;
import com.iusofts.agentplus.aiflow.entity.AiFlowVersion;
import com.iusofts.agentplus.aiflow.enums.PublishingStatusEnum;
import com.iusofts.agentplus.aiflow.mapper.AiFlowMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowVersionMapper;
import com.iusofts.agentplus.aiflow.utils.AiFlowVersionUtil;
import com.iusofts.agentplus.aiflow.vo.*;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.Knowledge;
import com.iusofts.agentplus.aiflow.vo.workflow.config.Model;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.common.vo.IdReqVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * AI流程版本 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Service
public class AiFlowVersionServiceImpl extends ServiceImpl<AiFlowVersionMapper, AiFlowVersion> implements IAiFlowVersionService {

    @Resource
    private AiFlowMapper aiFlowMapper;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWorkflow(AiFlowVersionSaveReqVo reqVo) {
        AiFlow aiFlow = aiFlowMapper.selectById(reqVo.getFlowId());
        if (aiFlow == null) {
            throw new SystemBusinessException("流程不存在");
        }

        if (reqVo.getId() != null) {
            // 编辑已有版本
            AiFlowVersion version = getById(reqVo.getId());
            if (version == null) {
                throw new SystemBusinessException("版本不存在");
            }
            // 已发布的版本不能编辑
            if (PublishingStatusEnum.PUBLISHED.getCode().equals(version.getPublishingStatus())) {
                throw new SystemBusinessException("已发布的版本不能编辑");
            }

            AiFlowVersion updateVersion = ModelMapperUtil.strictMap(reqVo, AiFlowVersion.class);
            updateVersion.setFlowJson(serializeWorkflow(reqVo.getWorkflow()));
            updateVersion.setUpdateBy(reqVo.getOperatorId());
            updateById(updateVersion);
        } else {
            // 新增版本 - 自动递增版本号
            String nextVersion = generateNextVersion(reqVo.getFlowId());

            AiFlowVersion version = ModelMapperUtil.strictMap(reqVo, AiFlowVersion.class);
            version.setFlowJson(serializeWorkflow(reqVo.getWorkflow()));
            version.setVersionNo(nextVersion);
            version.setCreateBy(reqVo.getOperatorId());
            version.setPublishingStatus(PublishingStatusEnum.DRAFT.getCode());
            save(version);

            // 更新流程的最新版本号
            aiFlow.setLatestVersion(nextVersion);
            aiFlow.setUpdateBy(reqVo.getOperatorId());
            aiFlowMapper.updateById(aiFlow);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(AiFlowVersionConfigUpdateReqVo reqVo) {
        AiFlowVersion version = getById(reqVo.getId());
        if (version == null) {
            throw new SystemBusinessException("版本不存在");
        }
        if (PublishingStatusEnum.PUBLISHED.getCode().equals(version.getPublishingStatus())) {
            throw new SystemBusinessException("已发布的版本不能编辑");
        }

        version.setConfigJson(serializeWorkflowConfig(reqVo.getConfig()));
        version.setUpdateBy(reqVo.getOperatorId());
        updateById(version);
    }

    @Override
    public PageResult<AiFlowVersionVo> queryPage(AiFlowVersionQueryPageReqVo reqVo) {
        PageResult<AiFlowVersionVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiFlowVersion> wrapper = Wrappers.lambdaQuery();

        if (reqVo.getFlowId() != null) {
            wrapper.eq(AiFlowVersion::getFlowId, reqVo.getFlowId());
        }
        if (reqVo.getPublishingStatus() != null) {
            wrapper.eq(AiFlowVersion::getPublishingStatus, reqVo.getPublishingStatus());
        }

        wrapper.select(AiFlowVersion.class, info -> !info.getColumn().equals("flow_json") && !info.getColumn().equals("config_json"));
        wrapper.orderByDesc(AiFlowVersion::getId);
        Page<AiFlowVersion> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        IPage<AiFlowVersion> page = page(pageParam, wrapper);

        List<AiFlowVersionVo> voList = page.getRecords().stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiFlowVersionVo.class))
                .toList();

        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public List<AiFlowVersionVo> queryByFlowId(Long flowId) {
        LambdaQueryWrapper<AiFlowVersion> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiFlowVersion::getFlowId, flowId);
        wrapper.select(AiFlowVersion.class, info -> !info.getColumn().equals("flow_json") && !info.getColumn().equals("config_json"));
        wrapper.orderByDesc(AiFlowVersion::getId);
        List<AiFlowVersion> list = list(wrapper);
        return list.stream()
                .map(item -> ModelMapperUtil.strictMap(item, AiFlowVersionVo.class))
                .toList();
    }

    @Override
    public void remove(IdReqVo reqVo) {
        AiFlowVersion version = getById(reqVo.getId());
        if (version == null) {
            throw new SystemBusinessException("版本不存在");
        }
        if (version.getPublishingStatus().equals(PublishingStatusEnum.PUBLISHED.getCode())) {
            throw new SystemBusinessException("已发布的版本不能删除");
        }
        removeById(reqVo.getId());
    }

    @Override
    public AiFlowVersionDetailVo getById(IdReqVo reqVo) {
        AiFlowVersion version = getById(reqVo.getId());
        if (version == null) {
            throw new SystemBusinessException("版本不存在");
        }
        AiFlowVersionDetailVo detailVo = ModelMapperUtil.strictMap(version, AiFlowVersionDetailVo.class);
        detailVo.setWorkflow(deserializeWorkflow(version.getFlowJson()));
        detailVo.setConfig(deserializeWorkflowConfig(version.getConfigJson()));
        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(AiFlowVersionPublishReqVo reqVo) {
        AiFlowVersion version = getById(reqVo.getId());
        if (version == null) {
            throw new SystemBusinessException("版本不存在");
        }

        AiFlow aiFlow = aiFlowMapper.selectById(version.getFlowId());
        if (aiFlow == null) {
            throw new SystemBusinessException("流程不存在");
        }

        // 更新版本状态
        version.setPublishingStatus(PublishingStatusEnum.PUBLISHED.getCode());
        version.setPublishingTime(LocalDateTime.now());
        version.setPublishingBy(reqVo.getOperatorId());
        version.setUpdateBy(reqVo.getOperatorId());
        updateById(version);

        // 更新流程的线上版本号
        aiFlow.setOnlineVersion(version.getVersionNo());
        aiFlow.setUpdateBy(reqVo.getOperatorId());
        aiFlowMapper.updateById(aiFlow);
    }

    @Override
    public AiFlowVersionDetailVo getWorkflowEditDetailByFlowId(Long flowId) {
        // 获取该流程的最新版本
        LambdaQueryWrapper<AiFlowVersion> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiFlowVersion::getFlowId, flowId);
        wrapper.orderByDesc(AiFlowVersion::getId);
        wrapper.last("LIMIT 1");
        AiFlowVersion latestVersion = getOne(wrapper);

        if (latestVersion == null) {
            // 没有版本，返回空详情
            AiFlowVersionDetailVo detailVo = new AiFlowVersionDetailVo();
            detailVo.setFlowId(flowId);
            detailVo.setConfig(deserializeWorkflowConfig(null));
            return detailVo;
        }

        AiFlowVersionDetailVo detailVo = ModelMapperUtil.strictMap(latestVersion, AiFlowVersionDetailVo.class);
        detailVo.setWorkflow(deserializeWorkflow(latestVersion.getFlowJson()));
        detailVo.setConfig(deserializeWorkflowConfig(latestVersion.getConfigJson()));

        // 如果是发布状态，不返回 id
        if (PublishingStatusEnum.PUBLISHED.getCode().equals(latestVersion.getPublishingStatus())) {
            detailVo.setId(null);
        }

        return detailVo;
    }

    @Override
    public List<Model> queryModelList() {
        // 模型列表暂时写死，未来改为查询独立的数据库表
        return List.of(
                buildModel(1L, "GPT-4o"),
                buildModel(2L, "GPT-4 Turbo"),
                buildModel(3L, "GPT-3.5 Turbo"),
                buildModel(4L, "Claude 3.5 Sonnet")
        );
    }

    @Override
    public List<Knowledge> queryKnowledgeList() {
        // 知识库列表暂时写死，未来改为查询独立的数据库表
        return List.of(
                buildKnowledge(1L, "知识库1"),
                buildKnowledge(2L, "知识库2"),
                buildKnowledge(3L, "知识库3")
        );
    }

    private String serializeWorkflow(Workflow workflow) {
        if (workflow == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(workflow);
        } catch (JsonProcessingException e) {
            throw new SystemBusinessException("流程数据解析失败");
        }
    }

    private Workflow deserializeWorkflow(String flowJson) {
        if (flowJson == null || flowJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(flowJson, Workflow.class);
        } catch (JsonProcessingException e) {
            throw new SystemBusinessException("流程数据解析失败");
        }
    }

    private String serializeWorkflowConfig(AiFlowVersionConfigUpdateReqVo.Config config) {
        if (config == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new SystemBusinessException("流程配置数据解析失败");
        }
    }

    private WorkflowConfig deserializeWorkflowConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return new WorkflowConfig();
        }
        try {
            return objectMapper.readValue(configJson, WorkflowConfig.class);
        } catch (JsonProcessingException e) {
            throw new SystemBusinessException("流程配置数据解析失败");
        }
    }

    private Model buildModel(Long id, String modelName) {
        Model model = new Model();
        model.setId(id);
        model.setModelName(modelName);
        return model;
    }

    private Knowledge buildKnowledge(Long id, String name) {
        Knowledge knowledge = new Knowledge();
        knowledge.setId(id);
        knowledge.setName(name);
        return knowledge;
    }

    private String generateNextVersion(Long flowId) {
        // 获取该流程的最新版本
        LambdaQueryWrapper<AiFlowVersion> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiFlowVersion::getFlowId, flowId);
        wrapper.orderByDesc(AiFlowVersion::getId);
        wrapper.last("LIMIT 1");
        AiFlowVersion latestVersion = getOne(wrapper);

        String currentVersion = latestVersion != null ? latestVersion.getVersionNo() : null;
        return AiFlowVersionUtil.generateNextVersion(currentVersion);
    }

}
