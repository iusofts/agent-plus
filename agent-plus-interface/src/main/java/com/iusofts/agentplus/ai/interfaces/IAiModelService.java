package com.iusofts.agentplus.ai.interfaces;

import com.iusofts.agentplus.ai.vo.model.AiModelAddReqVo;
import com.iusofts.agentplus.ai.vo.model.AiModelDetailVo;
import com.iusofts.agentplus.ai.vo.model.AiModelEditReqVo;
import com.iusofts.agentplus.ai.vo.model.AiModelQueryPageReqVo;
import com.iusofts.agentplus.ai.vo.model.AiModelVo;
import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;

import java.util.List;

/**
 * <p>
 * AI模型配置 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
public interface IAiModelService {

    void add(AiModelAddReqVo reqVo);

    void edit(AiModelEditReqVo reqVo);

    PageResult<AiModelVo> queryPage(AiModelQueryPageReqVo reqVo);

    /**
     * 按类型查询启用的模型列表(用于下拉选择)。
     *
     * @param orgId     组织ID
     * @param modelType 模型类型 1:LLM 2:Embedding,null 表示全部
     */
    List<AiModelVo> queryEnabled(Integer orgId, Integer modelType);

    void remove(IdReqVo reqVo);

    AiModelDetailVo getById(IdReqVo reqVo);

}
