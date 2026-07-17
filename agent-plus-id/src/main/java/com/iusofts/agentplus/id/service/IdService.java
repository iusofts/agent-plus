package com.iusofts.agentplus.id.service;

/**
 * 统一id生成器
 */
public interface IdService {

    /**
     * 生成ID
     *
     * @return 主键id
     */
    Integer generateUid(UidTypeEnum typeEnum);

    /**
     * ID类型枚举，
     */
    enum UidTypeEnum {

        CHAT(1, "Chat"),
        FLOW(2, "Flow"),
        KNOWLEDGE_BASE(3, "KnowledgeBase"),
        KNOWLEDGE_DOCUMENT(4, "KnowledgeDocument"),
        KNOWLEDGE_CHUNK(5, "KnowledgeChunk"),
        AI_MODEL(6, "AiModel"),
        TOOL(11, "Tool"),
        PLUGIN(12, "Plugin"),
        ;

        /**
         * 类型
         */
        Integer type;

        /**
         * 描述
         */
        String desc;

        UidTypeEnum(Integer type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public Integer getType() {
            return type;
        }

    }
}
