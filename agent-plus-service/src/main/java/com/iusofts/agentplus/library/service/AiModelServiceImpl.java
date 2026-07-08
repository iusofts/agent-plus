package com.iusofts.agentplus.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.library.entity.AiModel;
import com.iusofts.agentplus.library.interfaces.IAiModelService;
import com.iusofts.agentplus.library.mapper.AiModelMapper;
import com.iusofts.agentplus.library.vo.model.AiModelAddReqVo;
import com.iusofts.agentplus.library.vo.model.AiModelDetailVo;
import com.iusofts.agentplus.library.vo.model.AiModelEditReqVo;
import com.iusofts.agentplus.library.vo.model.AiModelQueryPageReqVo;
import com.iusofts.agentplus.library.vo.model.AiModelVo;
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
 * AI模型配置 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Service
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel> implements IAiModelService {

    @Resource
    private IdService idService;

    @Override
    public void add(AiModelAddReqVo reqVo) {
        AiModel aiModel = ModelMapperUtil.strictMap(reqVo, AiModel.class);
        Integer uid = idService.generateUid(UidTypeEnum.CHAT);
        aiModel.setId(uid.longValue());
        aiModel.setCreateBy(reqVo.getOperatorId());
        super.save(aiModel);
    }

    @Override
    public void edit(AiModelEditReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiModel aiModel = ModelMapperUtil.strictMap(reqVo, AiModel.class);
        // apiKey 传空则不更新原值(掩码回显场景)
        if (StringUtils.isBlank(reqVo.getApiKey())) {
            aiModel.setApiKey(null);
        }
        aiModel.setUpdateBy(reqVo.getOperatorId());
        super.updateById(aiModel);
    }

    @Override
    public PageResult<AiModelVo> queryPage(AiModelQueryPageReqVo reqVo) {
        PageResult<AiModelVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<AiModel> wrapper = Wrappers.lambdaQuery();
        if (reqVo.getOrgId() != null) {
            wrapper.eq(AiModel::getOrgId, reqVo.getOrgId());
        }
        if (reqVo.getModelType() != null) {
            wrapper.eq(AiModel::getModelType, reqVo.getModelType());
        }
        if (StringUtils.isNotBlank(reqVo.getProvider())) {
            wrapper.eq(AiModel::getProvider, reqVo.getProvider());
        }
        if (StringUtils.isNotBlank(reqVo.getModelName())) {
            wrapper.like(AiModel::getModelName, reqVo.getModelName());
        }
        if (reqVo.getStatus() != null) {
            wrapper.eq(AiModel::getStatus, reqVo.getStatus());
        }
        wrapper.orderByDesc(AiModel::getId);
        Page<AiModel> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        IPage<AiModel> page = super.page(pageParam, wrapper);
        List<AiModelVo> voList = page.getRecords().stream().map(item -> {
            AiModelVo vo = ModelMapperUtil.strictMap(item, AiModelVo.class);
            vo.setApiKey(maskApiKey(item.getApiKey()));
            return vo;
        }).toList();
        pageResult.setDataList(voList);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public List<AiModelVo> queryEnabled(Integer orgId, Integer modelType) {
        LambdaQueryWrapper<AiModel> wrapper = Wrappers.lambdaQuery();
        if (orgId != null) {
            wrapper.eq(AiModel::getOrgId, orgId);
        }
        if (modelType != null) {
            wrapper.eq(AiModel::getModelType, modelType);
        }
        wrapper.eq(AiModel::getStatus, 1);
        wrapper.orderByDesc(AiModel::getIsDefault).orderByDesc(AiModel::getId);
        List<AiModel> list = super.list(wrapper);
        return list.stream().map(item -> {
            AiModelVo vo = ModelMapperUtil.strictMap(item, AiModelVo.class);
            vo.setApiKey(maskApiKey(item.getApiKey()));
            return vo;
        }).toList();
    }

    @Override
    public void remove(IdReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiModel aiModel = super.getById(reqVo.getId());
        if (aiModel == null) {
            throw new SystemBusinessException("模型不存在");
        }
        super.removeById(reqVo.getId());
    }

    @Override
    public AiModelDetailVo getById(IdReqVo reqVo) {
        checkDataPermission(reqVo.getId(), reqVo.getOrgId());
        AiModel aiModel = super.getById(reqVo.getId());
        if (aiModel == null) {
            throw new SystemBusinessException("模型不存在");
        }
        AiModelDetailVo vo = ModelMapperUtil.strictMap(aiModel, AiModelDetailVo.class);
        vo.setApiKey(maskApiKey(aiModel.getApiKey()));
        return vo;
    }

    private void checkDataPermission(Long id, Integer orgId) {
        LambdaQueryWrapper<AiModel> checkWrapper = Wrappers.lambdaQuery();
        checkWrapper.eq(AiModel::getId, id);
        if (orgId != null) {
            checkWrapper.eq(AiModel::getOrgId, orgId);
        }
        long count = super.count(checkWrapper);
        if (count == 0) {
            throw new SystemBusinessException("操作权限获取失败！");
        }
    }

    /**
     * 掩码 apiKey:保留前 3 位与后 4 位,中间用 **** 代替。
     */
    private String maskApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            return apiKey;
        }
        int len = apiKey.length();
        if (len <= 7) {
            return "****";
        }
        return apiKey.substring(0, 3) + "****" + apiKey.substring(len - 4);
    }

}
