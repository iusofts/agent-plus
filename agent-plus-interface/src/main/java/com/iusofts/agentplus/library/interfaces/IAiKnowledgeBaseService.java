package com.iusofts.agentplus.library.interfaces;

import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseDetailVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseEditReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseQueryPageReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseVo;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;

import java.util.List;

/**
 * <p>
 * AI知识库 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
public interface IAiKnowledgeBaseService {

    Long add(AiKnowledgeBaseAddReqVo reqVo);

    void edit(AiKnowledgeBaseEditReqVo reqVo);

    PageResult<AiKnowledgeBaseVo> queryPage(AiKnowledgeBaseQueryPageReqVo reqVo);

    List<AiKnowledgeBaseVo> queryAll(Integer orgId);

    void remove(IdReqVo reqVo);

    AiKnowledgeBaseDetailVo getById(IdReqVo reqVo);

    /**
     * 重建知识库下所有文档的向量
     */
    void rebuildAllVectors(IdReqVo reqVo);

}
