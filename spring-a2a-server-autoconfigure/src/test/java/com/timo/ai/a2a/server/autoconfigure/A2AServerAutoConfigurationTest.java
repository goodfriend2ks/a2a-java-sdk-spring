package com.timo.ai.a2a.server.autoconfigure;

import com.timo.ai.a2a.server.context.CallContextFactory;
import com.timo.ai.a2a.server.context.DefaultCallContextFactory;
import com.timo.ai.a2a.server.executor.AgentExecutorHandler;
import com.timo.ai.a2a.server.rest.handler.AgentRestHandler;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.config.A2AConfigProvider;
import io.a2a.server.config.DefaultValuesConfigProvider;
import io.a2a.server.events.QueueManager;
import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.server.tasks.PushNotificationConfigStore;
import io.a2a.server.tasks.PushNotificationSender;
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.AgentCard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class A2AServerAutoConfigurationTest {

    @Mock
    private Environment environment;

    @Mock
    private AgentCard agentCard;

    @Mock
    private AgentExecutorHandler agentExecutorHandler;

    @Mock
    private AgentRestHandler agentRestHandler;

    @Mock
    private RequestHandler requestHandler;

    @Mock
    private TaskStore taskStore;

    @Mock
    private QueueManager queueManager;

    @Mock
    private PushNotificationConfigStore pushConfigStore;

    @Mock
    private PushNotificationSender pushSender;

    @Mock
    private Executor executor;

    private A2AServerAutoConfiguration configuration;

    @Test
    void testConfigurationInitialization() {
        configuration = new A2AServerAutoConfiguration();
        assertNotNull(configuration);
    }

    @Test
    void testLogAgentCard() {
        configuration = new A2AServerAutoConfiguration();
        when(agentCard.name()).thenReturn("TestAgent");
        when(agentCard.version()).thenReturn("1.0.0");

        configuration.logAgentCard(agentCard);

        verify(agentCard).name();
        verify(agentCard).version();
    }

    @Test
    void testDefaultValuesConfigProvider() {
        configuration = new A2AServerAutoConfiguration();

        DefaultValuesConfigProvider provider = configuration.defaultValuesConfigProvider();

        assertNotNull(provider);
    }

    @Test
    void testConfigProvider() {
        configuration = new A2AServerAutoConfiguration();
        DefaultValuesConfigProvider defaultProvider = new DefaultValuesConfigProvider();

        A2AConfigProvider configProvider = configuration.configProvider(environment, defaultProvider);

        assertNotNull(configProvider);
        assertInstanceOf(SpringA2AConfigProvider.class, configProvider);
    }

    @Test
    void testCallContextFactory() {
        configuration = new A2AServerAutoConfiguration();

        CallContextFactory factory = configuration.callContextFactory();

        assertNotNull(factory);
        assertInstanceOf(DefaultCallContextFactory.class, factory);
    }

    @Test
    void testAgentCardController() {
        configuration = new A2AServerAutoConfiguration();

        var controller = configuration.agentCardController(agentCard);

        assertNotNull(controller);
    }

    @Test
    void testTaskController() {
        configuration = new A2AServerAutoConfiguration();
        CallContextFactory callContextFactory = new DefaultCallContextFactory("");

        var controller = configuration.taskController(agentRestHandler, callContextFactory);

        assertNotNull(controller);
    }

    @Test
    void testTaskStore() {
        configuration = new A2AServerAutoConfiguration();

        TaskStore store = configuration.taskStore();

        assertNotNull(store);
    }

    @Test
    void testQueueManager() {
        configuration = new A2AServerAutoConfiguration();
        // Create a real TaskStore since the method casts it to TaskStateProvider
        TaskStore realTaskStore = configuration.taskStore();

        QueueManager manager = configuration.queueManager(realTaskStore);

        assertNotNull(manager);
    }

    @Test
    void testPushNotificationConfigStore() {
        configuration = new A2AServerAutoConfiguration();

        PushNotificationConfigStore store = configuration.pushNotificationConfigStore();

        assertNotNull(store);
    }

    @Test
    void testPushNotificationSender() {
        configuration = new A2AServerAutoConfiguration();

        PushNotificationSender sender = configuration.pushNotificationSender();

        assertNotNull(sender);
    }

    @Test
    void testA2AInternalExecutorWithDefaults() {
        configuration = new A2AServerAutoConfiguration();
        A2AConfigProvider configProvider = mock(A2AConfigProvider.class);

        when(configProvider.getValue(SpringA2AConfigProvider.A2A_EXECUTOR_CORE_POOL_SIZE))
                .thenReturn(String.valueOf(SpringA2AConfigProvider.DEFAULT_A2A_EXECUTOR_CORE_POOL_SIZE));
        when(configProvider.getValue(SpringA2AConfigProvider.A2A_EXECUTOR_MAX_POOL_SIZE))
                .thenReturn(String.valueOf(SpringA2AConfigProvider.DEFAULT_A2A_EXECUTOR_MAX_POOL_SIZE));
        when(configProvider.getValue(SpringA2AConfigProvider.A2A_EXECUTOR_KEEP_ALIVE_SECONDS))
                .thenReturn(String.valueOf(SpringA2AConfigProvider.DEFAULT_A2A_EXECUTOR_KEEP_ALIVE_SECONDS));

        Executor executor = configuration.a2aInternalExecutor(configProvider);

        assertNotNull(executor);
    }

    @Test
    void testA2AInternalExecutorWithCustomValues() {
        configuration = new A2AServerAutoConfiguration();
        A2AConfigProvider configProvider = mock(A2AConfigProvider.class);

        when(configProvider.getValue(SpringA2AConfigProvider.A2A_EXECUTOR_CORE_POOL_SIZE))
                .thenReturn("5");
        when(configProvider.getValue(SpringA2AConfigProvider.A2A_EXECUTOR_MAX_POOL_SIZE))
                .thenReturn("20");
        when(configProvider.getValue(SpringA2AConfigProvider.A2A_EXECUTOR_KEEP_ALIVE_SECONDS))
                .thenReturn("120");

        Executor executor = configuration.a2aInternalExecutor(configProvider);

        assertNotNull(executor);
    }

    @Test
    void testAgentExecutor() {
        configuration = new A2AServerAutoConfiguration();

        AgentExecutor agentExecutor = configuration.agentExecutor(agentExecutorHandler);

        assertNotNull(agentExecutor);
    }

    @Test
    void testRequestHandler() {
        configuration = new A2AServerAutoConfiguration();
        A2AConfigProvider configProvider = mock(A2AConfigProvider.class);
        AgentExecutor agentExecutor = mock(AgentExecutor.class);

        // Mock all the config values that are needed by the RequestHandler
        when(configProvider.getValue("a2a.blocking.agent.timeout.seconds")).thenReturn("60");
        when(configProvider.getValue("a2a.blocking.consumption.timeout.seconds")).thenReturn("30");

        RequestHandler handler = configuration.requestHandler(
                configProvider,
                agentExecutor,
                taskStore,
                queueManager,
                pushConfigStore,
                pushSender,
                executor
        );

        assertNotNull(handler);
    }

    @Test
    void testAgentRestHandler() {
        configuration = new A2AServerAutoConfiguration();

        AgentRestHandler handler = configuration.agentRestHandler(
                agentCard,
                requestHandler,
                executor
        );

        assertNotNull(handler);
    }
}
