package com.iusofts.agentplus.ai.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * AI知识库文档 批量添加请求对象
 * </p>
 *
 * <p>前端一次上传多个文件至 OSS 后,批量登记元数据。</p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
public class AiKnowledgeDocumentBatchAddReqVo {

    @NotNull(message = "知识库ID不能为空")
    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @Valid
    @NotEmpty(message = "文档列表不能为空")
    @Schema(description = "文档列表")
    private List<DocItem> documents;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

    @Data
    public static class DocItem {

        @Schema(description = "文档名称")
        private String name;

        @Schema(description = "文档类型(如 pdf/docx/txt)")
        private String docType;

        @Schema(description = "文档URL(阿里云OSS地址)")
        private String docUrl;

    }

}
