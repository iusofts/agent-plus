package com.iusofts.agentplus.library.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * AI知识库文档 添加请求对象
 * </p>
 *
 * <p>文件由前端上传至阿里云 OSS 后,把 url + 文件名提交到服务端,此处仅登记元数据,
 * 文档 status 初始为 0(待处理),等待后续向量化管线拉取并分块入库。</p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
public class AiKnowledgeDocumentAddReqVo {

    @NotNull(message = "知识库ID不能为空")
    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @NotBlank(message = "文档名称不能为空")
    @Schema(description = "文档名称")
    private String name;

    @Schema(description = "文档类型(如 pdf/docx/txt)")
    private String docType;

    @NotBlank(message = "文档URL不能为空")
    @Schema(description = "文档URL(阿里云OSS地址)")
    private String docUrl;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
