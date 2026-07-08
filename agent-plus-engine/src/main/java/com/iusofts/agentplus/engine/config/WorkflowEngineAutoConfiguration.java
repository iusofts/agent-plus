package com.iusofts.agentplus.engine.config;

import com.iusofts.agentplus.engine.WorkflowEngine;
import com.iusofts.agentplus.engine.knowledge.ChromaKnowledgeRetriever;
import com.iusofts.agentplus.engine.knowledge.ChromaProperties;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.knowledge.NoopKnowledgeRetriever;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import com.iusofts.agentplus.engine.llm.DefaultChatModelProvider;
import com.iusofts.agentplus.engine.llm.DoubaoProperties;
import com.iusofts.agentplus.engine.llm.QwenProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流引擎 Spring 自动装配。
 *
 * <p>业务模块只需配置千问/豆包 API Key，即可获得就绪的 {@link WorkflowEngine}。</p>
 *
 * @author Ivan
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({QwenProperties.class, DoubaoProperties.class, ChromaProperties.class})
public class WorkflowEngineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ChatModelProvider chatModelProvider(QwenProperties qwenProperties, DoubaoProperties doubaoProperties) {
        return new DefaultChatModelProvider(qwenProperties, doubaoProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chroma", name = "enabled", havingValue = "true")
    public KnowledgeRetriever knowledgeRetriever(ChromaProperties chromaProperties) {
        return new ChromaKnowledgeRetriever(chromaProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chroma", name = "enabled", havingValue = "false", matchIfMissing = true)
    public KnowledgeRetriever noopKnowledgeRetriever() {
        return new NoopKnowledgeRetriever();
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkflowEngine workflowEngine(ChatModelProvider chatModelProvider, ObjectProvider<KnowledgeRetriever> retriever) {
        return WorkflowEngine.builder()
                .chatModelProvider(chatModelProvider)
                .knowledgeRetriever(retriever.getIfAvailable(NoopKnowledgeRetriever::new))
                .build();
    }
}
