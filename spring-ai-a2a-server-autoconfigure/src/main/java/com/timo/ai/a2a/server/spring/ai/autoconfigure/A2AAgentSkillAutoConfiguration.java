package com.timo.ai.a2a.server.spring.ai.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timo.ai.a2a.processor.AgentSkillsProvider;
import com.timo.ai.a2a.rest.controller.AgentSkillController;
import com.timo.ai.a2a.server.context.CallContextFactory;
import com.timo.ai.a2a.service.AgentSkillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(
        prefix = A2AAgentSkillProperties.CONFIG_PREFIX,
        name = "enabled",
        havingValue = "true",
        matchIfMissing = A2AAgentSkillProperties.DEFAULT_A2A_AGENT_SKILL_ENABLED
)
@EnableConfigurationProperties(A2AAgentSkillProperties.class)
public class A2AAgentSkillAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(A2AAgentSkillAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    AgentSkillsProvider agentSkillsProvider(
            ApplicationContext applicationContext
    ) {
        LOGGER.info("Creating AgentSkillsProvider bean");

        return new AgentSkillsProvider(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    AgentSkillService agentSkillService(
            AgentSkillsProvider agentSkillsProvider,
            ObjectMapper objectMapper
    ) {
        LOGGER.info("Creating AgentSkillService bean");

        return new AgentSkillService(
                agentSkillsProvider,
                objectMapper
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = A2AAgentSkillProperties.CONFIG_PREFIX,
            name = { "enabled", A2AAgentSkillProperties.CONFIG_REST_INFIX + ".enabled" },
            havingValue = "true",
            matchIfMissing = A2AAgentSkillProperties.DEFAULT_A2A_AGENT_SKILL_ENABLED
                    && A2AAgentSkillProperties.DEFAULT_A2A_AGENT_SKILL_REST_ENABLED
    )
    AgentSkillController agentSkillRestController(
            AgentSkillService agentSkillService,
            CallContextFactory callContextFactory
    ) {
        LOGGER.info("Creating AgentSkillController bean");

        return new AgentSkillController(
                agentSkillService,
                callContextFactory
        );
    }
}
