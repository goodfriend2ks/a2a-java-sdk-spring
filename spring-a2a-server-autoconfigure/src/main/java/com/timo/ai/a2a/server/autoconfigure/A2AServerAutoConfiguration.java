package com.timo.ai.a2a.server.autoconfigure;

import com.timo.ai.a2a.server.card.AgentCardController;
import com.timo.ai.a2a.server.context.CallContextFactory;
import com.timo.ai.a2a.server.context.DefaultCallContextFactory;
import com.timo.ai.a2a.server.executor.AgentExecutorHandler;
import com.timo.ai.a2a.server.executor.DefaultAgentExecutor;
import com.timo.ai.a2a.server.rest.controller.TaskController;
import com.timo.ai.a2a.server.rest.handler.AgentRestHandler;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.config.A2AConfigProvider;
import io.a2a.server.config.DefaultValuesConfigProvider;
import io.a2a.server.events.InMemoryQueueManager;
import io.a2a.server.events.QueueManager;
import io.a2a.server.requesthandlers.AgentRequestHandler;
import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.server.tasks.InMemoryPushNotificationConfigStore;
import io.a2a.server.tasks.InMemoryTaskStore;
import io.a2a.server.tasks.PushNotificationConfigStore;
import io.a2a.server.tasks.PushNotificationSender;
import io.a2a.server.tasks.TaskStateProvider;
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.AgentCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Spring Boot auto-configuration for A2A Server.
 * <p>
 * Provides Rest APIs for A2A agent card metadata, and task.
 *
 * @author Timo
 * @since 0.1.0
 */
@AutoConfigureAfter(SpringA2AServerAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = A2AServerProperties.CONFIG_PREFIX,
        name = "enabled",
        havingValue = "true",
        matchIfMissing = A2AServerProperties.DEFAULT_A2A_SERVER_ENABLED
)
@EnableConfigurationProperties(A2AServerProperties.class)
public class A2AServerAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(A2AServerAutoConfiguration.class);

    A2AServerAutoConfiguration() {
        LOGGER.info("{} initiated", A2AServerAutoConfiguration.class.getSimpleName());
    }

    /**
     * Log [AgentCard] at startup. Applications MUST provide {@link AgentCard} bean.
     */
    @Autowired
    void logAgentCard(AgentCard agentCard) {
        LOGGER.info("Using AgentCard: {} (version: {})", agentCard.name(), agentCard.version());
    }

    @Bean
    DefaultValuesConfigProvider defaultValuesConfigProvider() {
        return new DefaultValuesConfigProvider();
    }

    /**
     * Configuration provider for A2A settings.
     * If a property is not found in the Spring {@link Environment},
     * it falls back to default values provided by {@link DefaultValuesConfigProvider}.
     */
    @Bean
    A2AConfigProvider configProvider(
            Environment environment,
            DefaultValuesConfigProvider defaultValuesConfigProvider
    ) {
        LOGGER.info("Auto-configuring SpringA2AConfigProvider for configuration");
        return new SpringA2AConfigProvider(environment, defaultValuesConfigProvider);
    }

    /**
     * Provide default {@link CallContextFactory} ({@link DefaultCallContextFactory} with empty tenant uid)
     */
    @Bean
    @ConditionalOnMissingBean
    CallContextFactory callContextFactory() {
        return new DefaultCallContextFactory("");
    }

    /**
     * Default Rest API for agent card metadata.
     * <p>
     * Note: The Application must provide {@link AgentCard} bean.
     */
    @Bean
    @ConditionalOnMissingBean
    AgentCardController agentCardController(AgentCard agentCard) {
        return new AgentCardController(agentCard);
    }

    /**
     * Default Rest API for task management.
     * <p>
     * Note: The Application must provide {@link CallContextFactory} bean
     * or using {@link DefaultCallContextFactory} with empty tenant uid.
     */
    @Bean
    @ConditionalOnMissingBean
    TaskController taskController(
            AgentRestHandler agentRestHandler,
            CallContextFactory callContextFactory
    ) {
        return new TaskController(agentRestHandler, callContextFactory);
    }

    /**
     * Provide default {@link TaskStore} ({@link InMemoryTaskStore}).
     */
    @Bean
    @ConditionalOnMissingBean
    TaskStore taskStore() {
        LOGGER.info("Auto-configuring InMemoryTaskStore for task management");
        return new InMemoryTaskStore();
    }

    /**
     * Provide default {@link QueueManager} ({@link InMemoryQueueManager}).
     */
    @Bean
    @ConditionalOnMissingBean
    QueueManager queueManager(TaskStore taskStore) {
        LOGGER.info("Auto-configuring InMemoryQueueManager for event queue management");
        return new InMemoryQueueManager((TaskStateProvider) taskStore);
    }

    /**
     * Provide default {@link PushNotificationConfigStore} ({@link InMemoryPushNotificationConfigStore}).
     */
    @Bean
    @ConditionalOnMissingBean
    PushNotificationConfigStore pushNotificationConfigStore() {
        LOGGER.info("Auto-configuring InMemoryPushNotificationConfigStore");
        return new InMemoryPushNotificationConfigStore();
    }

    /**
     * Provide default {@link PushNotificationSender} (no-op).
     */
    @Bean
    @ConditionalOnMissingBean
    PushNotificationSender pushNotificationSender() {
        LOGGER.info("Auto-configuring no-op PushNotificationSender (override to enable)");

        return task -> LOGGER.debug("Push notification requested for task {} but sender is disabled", task.id());
    }

    /**
     * Provide internal executor for async agent operations.
     */
    @Bean("a2aInternal")
    @ConditionalOnMissingBean(name = "a2aInternalExecutor")
    Executor a2aInternalExecutor(A2AConfigProvider configProvider) {
        var corePoolSize = Optional.of(configProvider.getValue(SpringA2AConfigProvider.A2A_EXECUTOR_CORE_POOL_SIZE))
                .map(Integer::parseInt)
                .orElse(SpringA2AConfigProvider.DEFAULT_A2A_EXECUTOR_CORE_POOL_SIZE);
        var maxPoolSize = Optional.of(configProvider.getValue(SpringA2AConfigProvider.A2A_EXECUTOR_MAX_POOL_SIZE))
                .map(Integer::parseInt)
                .orElse(SpringA2AConfigProvider.DEFAULT_A2A_EXECUTOR_MAX_POOL_SIZE);
        var keepAliveSeconds = Optional.of(
                        configProvider.getValue(SpringA2AConfigProvider.A2A_EXECUTOR_KEEP_ALIVE_SECONDS)
                ).map(Long::parseLong)
                .orElse(SpringA2AConfigProvider.DEFAULT_A2A_EXECUTOR_KEEP_ALIVE_SECONDS);

        LOGGER.info(
                "Creating A2A internal executor: corePoolSize={}, maxPoolSize={}, keepAliveSeconds={}",
                corePoolSize, maxPoolSize, keepAliveSeconds
        );

        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                runnable -> Thread.ofPlatform()
                        .daemon(false)
                        .name("a2a-agent-executor-", 1L)
                        .unstarted(runnable)
        );
    }

    /**
     * Provide default {@link AgentExecutor} ({@link DefaultAgentExecutor})
     * <p>
     * Note: Application must provide {@link AgentExecutorHandler} bean.
     */
    @Bean
    @ConditionalOnMissingBean
    AgentExecutor agentExecutor(AgentExecutorHandler agentExecutorHandler) {
        return new DefaultAgentExecutor(agentExecutorHandler);
    }

    /**
     * Provide {@link RequestHandler} wiring all A2A SDK components together.
     * <p>
     * Note: Applications must provide their own {@link AgentExecutor} bean
     * or using default bean {@link DefaultAgentExecutor} with own {@link AgentExecutorHandler}.
     */
    @Bean
    @ConditionalOnMissingBean
    RequestHandler requestHandler(
            A2AConfigProvider configProvider,
            AgentExecutor agentExecutor,
            TaskStore taskStore,
            QueueManager queueManager,
            PushNotificationConfigStore pushConfigStore,
            PushNotificationSender pushSender,
            @Qualifier("a2aInternal") Executor executor
    ) {
        LOGGER.info("Creating DefaultSpringRequestHandler with A2A SDK components");

        return new AgentRequestHandler(
                agentExecutor,
                taskStore,
                queueManager,
                pushConfigStore,
                pushSender,
                executor,
                configProvider
        );
    }

    /**
     * Provide {@link AgentRestHandler} to handle Rest requests.
     */
    @Bean
    @ConditionalOnMissingBean
    AgentRestHandler agentRestHandler(
            AgentCard agentCard,
            RequestHandler requestHandler,
            @Qualifier("a2aInternal") Executor executor
    ) {
        LOGGER.info("Creating default AgentRestHandler for Restful A2A server");
        return new AgentRestHandler(agentCard, requestHandler, executor);
    }
}
