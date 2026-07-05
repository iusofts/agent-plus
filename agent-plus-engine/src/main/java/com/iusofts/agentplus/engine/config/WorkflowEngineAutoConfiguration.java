package com.iusofts.agentplus.engine.config;

import com.iusofts.agentplus.engine.WorkflowEngine;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.knowledge.NoopKnowledgeRetriever;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流引擎 Spring 自动装配。
 *
 * <p>业务模块只需提供 {@link ChatModelProvider} Bean(可选注入
 * {@link KnowledgeRetriever}),即可获得就绪的 {@link WorkflowEngine}。</p>
 *
 * @author Ivan
 */
@Configuration(proxyBeanMethods = false)
public class WorkflowEngineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KnowledgeRetriever knowledgeRetriever() {
        return new NoopKnowledgeRetriever();
    }

    @Bean
    @ConditionalOnBean(ChatModelProvider.class)
    @ConditionalOnMissingBean
    public WorkflowEngine workflowEngine(ChatModelProvider chatModelProvider,
                                         ObjectProvider<KnowledgeRetriever> retriever) {
        return WorkflowEngine.builder()
                .chatModelProvider(chatModelProvider)
                .knowledgeRetriever(retriever.getIfAvailable(NoopKnowledgeRetriever::new))
                .build();
    }
}
