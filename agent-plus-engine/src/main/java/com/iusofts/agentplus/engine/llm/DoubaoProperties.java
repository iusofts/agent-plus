package com.iusofts.agentplus.engine.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 豆包（火山引擎）模型配置。
 *
 * @author Ivan
 */
@ConfigurationProperties(prefix = "doubao")
public class DoubaoProperties {

    /**
     * 豆包 API Key.
     */
    private String apiKey;

    /**
     * 豆包 API 基础地址.
     * 默认: https://ark.cn-beijing.volces.com/api/v3
     */
    private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";

    /**
     * 默认模型端点 ID.
     */
    private String model;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
