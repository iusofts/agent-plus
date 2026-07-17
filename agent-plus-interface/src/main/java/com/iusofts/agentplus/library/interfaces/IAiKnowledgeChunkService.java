package com.iusofts.agentplus.library.interfaces;

import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkEditReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkQueryPageReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkStatusReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkVo;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;

/**
 * <p>
 * AI知识库文档分块 服务类
 * </p>
 *
 * @author Ivan
 * @since 2026-07-09
 */
public interface IAiKnowledgeChunkService {

    PageResult<AiKnowledgeChunkVo> queryPage(AiKnowledgeChunkQueryPageReqVo reqVo);

    Long add(AiKnowledgeChunkAddReqVo reqVo);

    AiKnowledgeChunkVo getById(IdReqVo reqVo);

    void edit(AiKnowledgeChunkEditReqVo reqVo);

    void changeStatus(AiKnowledgeChunkStatusReqVo reqVo);

    void remove(IdReqVo reqVo);

}
