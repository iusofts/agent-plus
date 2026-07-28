package com.iusofts.agentplus.engine.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云百炼（DashScope）模型配置。
 *
 * @author Ivan
 */
@ConfigurationProperties(prefix = "dashscope")
public class DashscopeProperties {

    /**
     * 阿里云百炼 API Key.
     */
    private String apiKey;

    /**
     * 阿里云百炼 API 基础地址.
     * 默认: https://dashscope.aliyuncs.com/compatible-mode/v1
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * 默认模型名称.
     * 默认: qwen-plus
     */
    private String model = "qwen-plus";

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
