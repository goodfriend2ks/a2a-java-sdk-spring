package com.timo.ai.a2a.server.autoconfigure;

import com.timo.ai.a2a.server.context.CallContextFactory;
import com.timo.ai.a2a.server.context.DefaultCallContextFactory;
import com.timo.ai.a2a.server.jsonrpc.controller.MessageController;
import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.spec.AgentCard;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class A2AJsonRpcServerAutoConfigurationTest {

    @Mock
    private AgentCard agentCard;

    @Mock
    private RequestHandler requestHandler;

    @Mock
    private Executor executor;

    @Mock
    private JSONRPCHandler jsonRpcHandler;

    private A2AJsonRpcServerAutoConfiguration configuration;

    @Test
    void testConfigurationInitialization() {
        configuration = new A2AJsonRpcServerAutoConfiguration();
        assertNotNull(configuration);
    }

    @Test
    void testAgentJsonRpcHandler() {
        configuration = new A2AJsonRpcServerAutoConfiguration();

        JSONRPCHandler handler = configuration.agentJsonRpcHandler(
                agentCard,
                requestHandler,
                executor
        );

        assertNotNull(handler);
    }

    @Test
    void testMessageJsonRpcController() {
        configuration = new A2AJsonRpcServerAutoConfiguration();
        CallContextFactory callContextFactory = new DefaultCallContextFactory("");

        MessageController controller = configuration.messageJsonRpcController(
                jsonRpcHandler,
                callContextFactory,
                executor
        );

        assertNotNull(controller);
    }

    @Test
    void testAgentJsonRpcHandlerCreatesHandlerSuccessfully() {
        configuration = new A2AJsonRpcServerAutoConfiguration();

        JSONRPCHandler handler = configuration.agentJsonRpcHandler(
                agentCard,
                requestHandler,
                executor
        );

        assertNotNull(handler);
    }
}
