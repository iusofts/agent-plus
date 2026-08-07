package com.iusofts.agentplus.trace.constants;

/**
 * 调用来源枚举。
 *
 * <p>统一定义 OTel Span Attribute / Baggage key {@code ai.call_source} 与
 * LLM / 知识库日志表 {@code call_source} 列的合法取值。</p>
 *
 * <p>对外仍以 {@link #getCode()} 返回的字符串字面量落库与跨服务传输（保持 DB 兼容），
 * 枚举仅在 Java 侧提供类型安全与集中管理。新增来源请在此追加，禁止在业务代码中
 * 直接使用魔法字符串。</p>
 *
 * @author Ivan
 * @since 2026-08-07
 */
public enum CallSource {

    /** 智能体对话入口 */
    AGENT("AGENT"),
    /** 会话式聊天 */
    CHAT("CHAT"),
    /** 工作流执行 */
    FLOW("FLOW"),
    /** 外部 API 调用 */
    API("API"),
    /** 知识库索引写入（embedding 入库） */
    EMBED_INDEX("EMBED_INDEX"),
    /** 知识库检索（embedding 召回） */
    EMBED_RETRIEVE("EMBED_RETRIEVE");

    private final String code;

    CallSource(String code) {
        this.code = code;
    }

    /** 字面量（落库 / 跨服务传输用）。 */
    public String getCode() {
        return code;
    }

    /**
     * 把字符串字面量反解析为枚举。用于反序列化 DB / 外部入参；
     * 大小写敏感，未匹配或入参为空时返回 {@code null}。
     */
    public static CallSource fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (CallSource v : values()) {
            if (v.code.equals(code)) {
                return v;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return code;
    }
}
