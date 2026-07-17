package com.iusofts.agentplus.library.interfaces;

import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentBatchAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentQueryPageReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentStatusReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentVo;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
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

    void changeStatus(AiKnowledgeDocumentStatusReqVo reqVo);

    AiKnowledgeDocumentVo getById(IdReqVo reqVo);

    /**
     * 重建文档向量（用 DB 保存的分块内容重新向量化）
     */
    void rebuildVector(IdReqVo reqVo);

    /**
     * 知识库启停时联动其下文档的启用状态。
     *
     * <p>停用:将知识库下所有「可用」文档置为「已禁用」,并删除其向量;
     * 启用:将知识库下所有「已禁用」文档恢复为「可用」,并重建其向量。
     * 已归档/失败/处理中的文档不受影响。</p>
     *
     * @param knowledgeBaseId 知识库ID
     * @param enable          true=启用 false=停用
     * @param operatorId      操作人ID
     */
    void cascadeStatusByKnowledgeBase(Long knowledgeBaseId, boolean enable, Long operatorId);

}
