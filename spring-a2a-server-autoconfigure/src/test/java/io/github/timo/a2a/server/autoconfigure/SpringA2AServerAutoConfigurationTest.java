package io.github.timo.a2a.server.autoconfigure;

import io.github.timo.a2a.server.SpringA2AServerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringA2AServerAutoConfigurationTest {

    @Mock
    private Environment environment;

    private SpringA2AServerAutoConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new SpringA2AServerAutoConfiguration();
    }

    @Test
    void testServletWebServerPropertiesWithDefaults() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("");

        SpringA2AServerProperties properties = configuration.servletWebServerProperties(environment);

        assertNotNull(properties);
        assertEquals(8080, properties.serverPort());
        assertEquals("", properties.contextPath());
        assertFalse(properties.sslEnabled());
        assertNotNull(properties.a2aServerHost());
        assertNotNull(properties.a2aServerGrpcHost());
        assertEquals(9091, properties.a2aServerGrpcPort());
        assertFalse(properties.a2aServerSSLEnabled());
        assertFalse(properties.a2aServerGrpcEnabled());
        assertFalse(properties.a2aServerJsonRpcEnabled());
        assertFalse(properties.a2aServerRestEnabled());
    }

    @Test
    void testServletWebServerPropertiesWithContextPath() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("/api/");

        SpringA2AServerProperties properties = configuration.servletWebServerProperties(environment);

        assertNotNull(properties);
        assertEquals("/api", properties.contextPath());
    }

    @Test
    void testServletWebServerPropertiesWithSSL() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("");
        when(environment.getProperty("server.ssl.enabled", Boolean.class, false)).thenReturn(true);

        SpringA2AServerProperties properties = configuration.servletWebServerProperties(environment);

        assertNotNull(properties);
        assertTrue(properties.sslEnabled());
        assertTrue(properties.a2aServerSSLEnabled());
    }

    @Test
    void testServletWebServerPropertiesWithA2AServerSSLTrue() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("");
        when(environment.getProperty(A2AServerProperties.CONFIG_PREFIX + ".ssl.enabled", "")).thenReturn("true");

        SpringA2AServerProperties properties = configuration.servletWebServerProperties(environment);

        assertNotNull(properties);
        assertTrue(properties.a2aServerSSLEnabled());
    }

    @Test
    void testServletWebServerPropertiesWithA2AServerSSLFalse() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("");
        when(environment.getProperty(A2AServerProperties.CONFIG_PREFIX + ".ssl.enabled", "")).thenReturn("false");

        SpringA2AServerProperties properties = configuration.servletWebServerProperties(environment);

        assertNotNull(properties);
        assertFalse(properties.a2aServerSSLEnabled());
    }

    @Test
    void testServletWebServerPropertiesWithCustomA2AHost() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("");
        when(environment.getProperty(A2AServerProperties.CONFIG_PREFIX + ".host",
                A2AServerProperties.DEFAULT_A2A_SERVER_HOST)).thenReturn("custom-host");

        SpringA2AServerProperties properties = configuration.servletWebServerProperties(environment);

        assertNotNull(properties);
        assertEquals("custom-host", properties.a2aServerHost());
    }

    @Test
    void testServletWebServerPropertiesWithGrpcEnabled() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("");
        when(environment.getProperty("spring.grpc.server.enabled", Boolean.class, true)).thenReturn(true);
        when(environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + "." + A2AServerProperties.CONFIG_GRPC_INFIX + ".enabled",
                Boolean.class,
                A2AServerProperties.DEFAULT_CONFIG_GRPC_ENABLED
        )).thenReturn(true);

        SpringA2AServerProperties properties = configuration.servletWebServerProperties(environment);

        assertNotNull(properties);
        assertTrue(properties.a2aServerGrpcEnabled());
    }

    @Test
    void testServletWebServerPropertiesWithJsonRpcEnabled() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("");
        when(environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + "." + A2AServerProperties.CONFIG_JSON_RPC_INFIX + ".enabled",
                Boolean.class,
                A2AServerProperties.DEFAULT_CONFIG_JSON_RPC_ENABLED
        )).thenReturn(true);

        SpringA2AServerProperties properties = configuration.servletWebServerProperties(environment);

        assertNotNull(properties);
        assertTrue(properties.a2aServerJsonRpcEnabled());
    }

    @Test
    void testServletWebServerPropertiesWithRestEnabled() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("");
        when(environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + "." + A2AServerProperties.CONFIG_REST_INFIX + ".enabled",
                Boolean.class,
                A2AServerProperties.DEFAULT_CONFIG_REST_ENABLED
        )).thenReturn(true);

        SpringA2AServerProperties properties = configuration.servletWebServerProperties(environment);

        assertNotNull(properties);
        assertTrue(properties.a2aServerRestEnabled());
    }

    @Test
    void testReactiveWebServerPropertiesWithDefaults() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("spring.webflux.base-path", "")).thenReturn("");

        SpringA2AServerProperties properties = configuration.reactiveWebServerProperties(environment);

        assertNotNull(properties);
        assertEquals(8080, properties.serverPort());
        assertEquals("", properties.contextPath());
        assertFalse(properties.sslEnabled());
    }

    @Test
    void testReactiveWebServerPropertiesWithContextPath() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("spring.webflux.base-path", "")).thenReturn("/api/v1/");

        SpringA2AServerProperties properties = configuration.reactiveWebServerProperties(environment);

        assertNotNull(properties);
        assertEquals("/api/v1", properties.contextPath());
    }

    @Test
    void testGrpcServerHostNameResolution() {
        setupDefaultEnvironmentMocks();
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("");
        when(environment.getProperty("spring.grpc.server.host", "")).thenReturn("specific-grpc-host");

        SpringA2AServerProperties properties = configuration.servletWebServerProperties(environment);

        assertNotNull(properties);
        assertEquals("specific-grpc-host", properties.a2aServerGrpcHost());
    }

    private void setupDefaultEnvironmentMocks() {
        when(environment.getProperty("server.port", Integer.class, 8080)).thenReturn(8080);
        when(environment.getProperty("server.ssl.enabled", Boolean.class, false)).thenReturn(false);
        when(environment.getProperty("spring.grpc.server.enabled", Boolean.class, true)).thenReturn(true);
        when(environment.getProperty("spring.grpc.server.host", "")).thenReturn("");
        when(environment.getProperty("spring.grpc.server.port", Integer.class, 9091)).thenReturn(9091);
        when(environment.getProperty(A2AServerProperties.CONFIG_PREFIX + ".host",
                A2AServerProperties.DEFAULT_A2A_SERVER_HOST)).thenReturn("*");
        when(environment.getProperty(A2AServerProperties.CONFIG_PREFIX + ".ssl.enabled", "")).thenReturn("");
        when(environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + "." + A2AServerProperties.CONFIG_GRPC_INFIX + ".enabled",
                Boolean.class,
                A2AServerProperties.DEFAULT_CONFIG_GRPC_ENABLED
        )).thenReturn(false);
        when(environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + "." + A2AServerProperties.CONFIG_JSON_RPC_INFIX + ".enabled",
                Boolean.class,
                A2AServerProperties.DEFAULT_CONFIG_JSON_RPC_ENABLED
        )).thenReturn(false);
        when(environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + "." + A2AServerProperties.CONFIG_REST_INFIX + ".enabled",
                Boolean.class,
                A2AServerProperties.DEFAULT_CONFIG_REST_ENABLED
        )).thenReturn(false);
    }
}
