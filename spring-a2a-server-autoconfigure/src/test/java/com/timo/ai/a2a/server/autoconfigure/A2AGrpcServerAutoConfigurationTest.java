package com.timo.ai.a2a.server.autoconfigure;

import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.spec.AgentCard;
import io.a2a.transport.grpc.handler.CallContextFactory;
import io.a2a.transport.grpc.handler.GrpcHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class A2AGrpcServerAutoConfigurationTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private AgentCard agentCard;

    @Mock
    private RequestHandler requestHandler;

    @Mock
    private CallContextFactory callContextFactory;

    @Mock
    private Executor executor;

    private A2AGrpcServerAutoConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new A2AGrpcServerAutoConfiguration();
    }

    @Test
    void testConfigurationInitialization() {
        assertNotNull(configuration);
    }

    @Test
    void testAgentGrpcHandlerWithCallContextFactory() {
        when(applicationContext.getBean(CallContextFactory.class)).thenReturn(callContextFactory);

        GrpcHandler handler = configuration.agentGrpcHandler(
                applicationContext,
                agentCard,
                requestHandler,
                executor
        );

        assertNotNull(handler);
        verify(applicationContext).getBean(CallContextFactory.class);
    }

    @Test
    void testAgentGrpcHandlerWithoutCallContextFactory() {
        when(applicationContext.getBean(CallContextFactory.class))
                .thenThrow(new NoSuchBeanDefinitionException(CallContextFactory.class));

        GrpcHandler handler = configuration.agentGrpcHandler(
                applicationContext,
                agentCard,
                requestHandler,
                executor
        );

        assertNotNull(handler);
        verify(applicationContext).getBean(CallContextFactory.class);
    }

    @Test
    void testAgentGrpcHandlerWithGenericException() {
        when(applicationContext.getBean(CallContextFactory.class))
                .thenThrow(new RuntimeException("Test exception"));

        GrpcHandler handler = configuration.agentGrpcHandler(
                applicationContext,
                agentCard,
                requestHandler,
                executor
        );

        assertNotNull(handler);
        verify(applicationContext).getBean(CallContextFactory.class);
    }

    @Test
    void testAgentGrpcHandlerUsesProvidedDependencies() {
        when(applicationContext.getBean(CallContextFactory.class)).thenReturn(callContextFactory);

        GrpcHandler handler = configuration.agentGrpcHandler(
                applicationContext,
                agentCard,
                requestHandler,
                executor
        );

        assertNotNull(handler);
        verify(applicationContext).getBean(CallContextFactory.class);
        verifyNoInteractions(agentCard, requestHandler, executor);
    }

    @Test
    void testAgentGrpcHandlerCreationWithNullCallContextFactory() {
        when(applicationContext.getBean(CallContextFactory.class))
                .thenThrow(new IllegalStateException("Bean not available"));

        GrpcHandler handler = configuration.agentGrpcHandler(
                applicationContext,
                agentCard,
                requestHandler,
                executor
        );

        assertNotNull(handler);
    }
}
