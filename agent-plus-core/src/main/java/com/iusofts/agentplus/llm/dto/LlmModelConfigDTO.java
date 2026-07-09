package com.iusofts.agentplus.llm.dto;

import lombok.Data;

/**
 * LLM 生成参数配置 DTO。
 *
 * <p>与模型连接信息 {@link LlmModelDTO} 分离，仅承载单次调用的生成参数。
 * 后续新增 topP、stop、enableSearch 等参数只需在此扩展，
 * 无需改动 createChatModel / chat 方法签名。字段为 null 表示不设置，使用模型默认。</p>
 *
 * @author Ivan
 */
@Data
public class LlmModelConfigDTO {

    /**
     * 生成随机性 temperature。
     */
    private Double temperature;

    /**
     * 最大回复 token 数。
     */
    private Integer maxTokens;

    // 后续可扩展：topP、stop、enableSearch 等生成参数
}
