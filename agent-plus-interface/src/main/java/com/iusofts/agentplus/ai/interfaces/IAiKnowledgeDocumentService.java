package com.iusofts.agentplus.ai.interfaces;

import com.iusofts.agentplus.ai.vo.knowledge.AiKnowledgeDocumentAddReqVo;
import com.iusofts.agentplus.ai.vo.knowledge.AiKnowledgeDocumentBatchAddReqVo;
import com.iusofts.agentplus.ai.vo.knowledge.AiKnowledgeDocumentQueryPageReqVo;
import com.iusofts.agentplus.ai.vo.knowledge.AiKnowledgeDocumentVo;
import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;

/**
 * <p>
 * AI知识库文档 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
public interface IAiKnowledgeDocumentService {

    Long add(AiKnowledgeDocumentAddReqVo reqVo);

    void batchAdd(AiKnowledgeDocumentBatchAddReqVo reqVo);

    PageResult<AiKnowledgeDocumentVo> queryPage(AiKnowledgeDocumentQueryPageReqVo reqVo);

    void remove(IdReqVo reqVo);

    AiKnowledgeDocumentVo getById(IdReqVo reqVo);

}
