package io.github.timo.a2a.server.autoconfigure;

import io.github.timo.a2a.server.context.CallContextFactory;
import io.github.timo.a2a.server.context.DefaultCallContextFactory;
import io.github.timo.a2a.server.jsonrpc.controller.MessageController;
import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.spec.AgentCard;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

/**
 * Spring Boot auto-configuration for JSON-RPC A2A Server.
 * <p>
 * Provides JSON-RPC APIs for A2A messages.
 *
 * @author Timo
 * @since 0.1.0
 */
@AutoConfigureAfter(A2AServerAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = A2AServerProperties.CONFIG_PREFIX,
        name = {"enabled", A2AServerProperties.CONFIG_JSON_RPC_INFIX + ".enabled"},
        havingValue = "true",
        matchIfMissing = A2AServerProperties.DEFAULT_A2A_SERVER_ENABLED
                && A2AServerProperties.DEFAULT_CONFIG_JSON_RPC_ENABLED
)
public class A2AJsonRpcServerAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(A2AJsonRpcServerAutoConfiguration.class);

    /**
     * Provide {@link JSONRPCHandler} to handle JSON-RPC requests.
     */
    @Bean
    @ConditionalOnMissingBean
    JSONRPCHandler agentJsonRpcHandler(
            AgentCard agentCard,
            RequestHandler requestHandler,
            @Qualifier("a2aInternal") Executor executor
    ) {
        LOGGER.info("Creating default JSONRPCHandler for JSON-RPC A2A server");
        return new JSONRPCHandler(agentCard, requestHandler, executor);
    }

    /**
     * Default JSON-RPC API for A2A messages.
     * <p>
     * Note: The Application must provide {@link CallContextFactory} bean
     * or using {@link DefaultCallContextFactory} with empty tenant uid.
     */
    @Bean
    @ConditionalOnMissingBean
    MessageController messageJsonRpcController(
            JSONRPCHandler agentJsonRpcHandler,
            CallContextFactory callContextFactory,
            @Qualifier("a2aInternal") Executor executor
    ) {
        LOGGER.info("Creating MessageController for JSON-RPC A2A server");
        return new MessageController(
                agentJsonRpcHandler,
                callContextFactory,
                executor
        );
    }
}
