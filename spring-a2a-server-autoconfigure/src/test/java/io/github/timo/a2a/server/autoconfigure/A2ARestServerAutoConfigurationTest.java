package io.github.timo.a2a.server.autoconfigure;

import io.github.timo.a2a.server.context.CallContextFactory;
import io.github.timo.a2a.server.context.DefaultCallContextFactory;
import io.github.timo.a2a.server.rest.controller.MessageController;
import io.github.timo.a2a.server.rest.handler.AgentRestHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class A2ARestServerAutoConfigurationTest {

    @Mock
    private AgentRestHandler agentRestHandler;

    private A2ARestServerAutoConfiguration configuration;

    @Test
    void testConfigurationInitialization() {
        configuration = new A2ARestServerAutoConfiguration();
        assertNotNull(configuration);
    }

    @Test
    void testMessageRestController() {
        configuration = new A2ARestServerAutoConfiguration();
        CallContextFactory callContextFactory = new DefaultCallContextFactory("");

        MessageController controller = configuration.messageRestController(
                agentRestHandler,
                callContextFactory
        );

        assertNotNull(controller);
    }

    @Test
    void testMessageRestControllerWithCustomTenant() {
        configuration = new A2ARestServerAutoConfiguration();
        CallContextFactory callContextFactory = new DefaultCallContextFactory("tenant-123");

        MessageController controller = configuration.messageRestController(
                agentRestHandler,
                callContextFactory
        );

        assertNotNull(controller);
    }
}
