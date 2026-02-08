package com.timo.ai.a2a.server.autoconfigure;

import com.timo.ai.a2a.server.grpc.AgentGrpcHandler;
import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.spec.AgentCard;
import io.a2a.transport.grpc.handler.CallContextFactory;
import io.a2a.transport.grpc.handler.GrpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.server.service.GrpcService;

import java.util.concurrent.Executor;

/**
 * Spring Boot auto-configuration for GRPC A2A server.
 * <p>
 * Provides GRPC APIs for A2A messages.
 *
 * @author Timo
 * @since 0.1.0
 */
@AutoConfigureAfter(A2AServerAutoConfiguration.class)
@AutoConfigureBefore(name = {"org.springframework.grpc.autoconfigure.server.GrpcServerFactoryAutoConfiguration"})
@ConditionalOnProperty(
        prefix = A2AServerProperties.CONFIG_PREFIX,
        name = {"enabled", A2AServerProperties.CONFIG_GRPC_INFIX + ".enabled"},
        havingValue = "true",
        matchIfMissing = A2AServerProperties.DEFAULT_A2A_SERVER_ENABLED
                && A2AServerProperties.DEFAULT_CONFIG_GRPC_ENABLED
)
public class A2AGrpcServerAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(A2AGrpcServerAutoConfiguration.class);

    /**
     * Provide {@link GrpcHandler} to handle GRPC requests.
     */
    @Bean
    @GrpcService
    @ConditionalOnMissingBean
    GrpcHandler agentGrpcHandler(
            ApplicationContext applicationContext,
            AgentCard agentCard,
            RequestHandler requestHandler,
            @Qualifier("a2aInternal") Executor executor
    ) {
        LOGGER.info("Creating default GrpcHandler for GRPC A2A server");

        try {
            var callContextFactory = applicationContext.getBean(CallContextFactory.class);
            return new AgentGrpcHandler(agentCard, requestHandler, callContextFactory, executor);
        } catch (Exception ex) {
            LOGGER.info("Cannot find CallContextFactory bean for GRPC A2A server", ex);

            return new AgentGrpcHandler(agentCard, requestHandler, null, executor);
        }
    }
}
