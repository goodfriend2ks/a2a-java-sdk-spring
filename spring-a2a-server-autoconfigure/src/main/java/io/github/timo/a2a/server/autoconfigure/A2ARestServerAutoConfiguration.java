package io.github.timo.a2a.server.autoconfigure;

import io.github.timo.a2a.server.context.CallContextFactory;
import io.github.timo.a2a.server.rest.controller.MessageController;
import io.github.timo.a2a.server.rest.handler.AgentRestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for Rest A2A server.
 * <p>
 * Provides Rest APIs for A2A messages.
 *
 * @author Timo
 * @since 0.1.0
 */
@AutoConfigureAfter(A2AServerAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = A2AServerProperties.CONFIG_PREFIX,
        name = {"enabled", A2AServerProperties.CONFIG_REST_INFIX + ".enabled"},
        havingValue = "true",
        matchIfMissing = A2AServerProperties.DEFAULT_A2A_SERVER_ENABLED
                && A2AServerProperties.DEFAULT_CONFIG_REST_ENABLED
)
@EnableConfigurationProperties(A2AServerProperties.class)
public class A2ARestServerAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(A2ARestServerAutoConfiguration.class);

    /**
     * Default Rest API for A2A messages.
     * <p>
     * Note: The Application must provide [CallContextFactory] bean
     * or using [DefaultCallContextFactory] with empty tenant uid.
     */
    @Bean
    @ConditionalOnMissingBean
    MessageController messageRestController(
            AgentRestHandler agentRestHandler,
            CallContextFactory callContextFactory
    ) {
        LOGGER.info("Creating MessageController for Rest A2A server");
        return new MessageController(agentRestHandler, callContextFactory);
    }
}
