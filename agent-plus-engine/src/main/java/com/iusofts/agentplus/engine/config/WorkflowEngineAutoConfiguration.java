package com.iusofts.agentplus.engine.config;

import com.iusofts.agentplus.engine.WorkflowEngine;
import com.iusofts.agentplus.engine.history.HistoryMessageProvider;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.knowledge.NoopKnowledgeRetriever;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import com.iusofts.agentplus.engine.llm.DefaultChatModelProvider;
import com.iusofts.agentplus.engine.llm.VolcengineProperties;
import com.iusofts.agentplus.engine.llm.DashscopeProperties;
import com.iusofts.agentplus.engine.tool.ToolRegistry;
import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流引擎 Spring 自动装配。
 *
 * <p>业务模块只需配置千问/豆包 API Key，即可获得就绪的 {@link WorkflowEngine}。</p>
 *
 * <p>知识库检索默认走 {@link NoopKnowledgeRetriever}（返回空）。业务模块(agent-plus-service)
 * 接入向量库后会提供 {@code @Primary} 的 {@link KnowledgeRetriever} 覆盖之。</p>
 *
 * @author Ivan
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({DashscopeProperties.class, VolcengineProperties.class})
public class WorkflowEngineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ChatModelProvider chatModelProvider(DashscopeProperties dashscopeProperties, VolcengineProperties volcengineProperties) {
        return new DefaultChatModelProvider(dashscopeProperties, volcengineProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public KnowledgeRetriever noopKnowledgeRetriever() {
        return new NoopKnowledgeRetriever();
    }

    @Bean
    @ConditionalOnMissingBean
    public ToolRegistry toolRegistry(ObjectProvider<Tool> tools, ToolQueryProvider toolQueryProvider) {
        ToolRegistry registry = new ToolRegistry(toolQueryProvider);
        tools.orderedStream().forEach(registry::register);
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkflowEngine workflowEngine(ChatModelProvider chatModelProvider,
                                         ObjectProvider<KnowledgeRetriever> retriever,
                                         ObjectProvider<ToolRegistry> toolRegistry,
                                         ObjectProvider<ToolQueryProvider> toolQueryProvider,
                                         ObjectProvider<HistoryMessageProvider> historyMessageProvider) {
        WorkflowEngine.Builder builder = WorkflowEngine.builder()
                .chatModelProvider(chatModelProvider)
                .knowledgeRetriever(retriever.getIfAvailable(NoopKnowledgeRetriever::new));

        toolRegistry.ifAvailable(builder::toolRegistry);
        toolQueryProvider.ifAvailable(builder::toolQueryProvider);
        historyMessageProvider.ifAvailable(builder::historyMessageProvider);

        return builder.build();
    }
}
