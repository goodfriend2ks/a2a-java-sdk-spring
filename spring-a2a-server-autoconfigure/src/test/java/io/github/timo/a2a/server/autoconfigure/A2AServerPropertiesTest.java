package io.github.timo.a2a.server.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class A2AServerPropertiesTest {

    @Test
    void testDefaultValues() {
        A2AServerProperties properties = new A2AServerProperties();

        assertTrue(properties.isEnabled());
        assertEquals(A2AServerProperties.DEFAULT_A2A_SERVER_HOST, properties.getHost());
        assertNull(properties.getSsl());
        assertNotNull(properties.getGrpc());
        assertFalse(properties.getGrpc().enabled());
        assertNotNull(properties.getJsonrpc());
        assertFalse(properties.getJsonrpc().enabled());
        assertNotNull(properties.getRest());
        assertFalse(properties.getRest().enabled());
    }

    @Test
    void testSettersAndGetters() {
        A2AServerProperties properties = new A2AServerProperties();

        properties.setEnabled(false);
        assertFalse(properties.isEnabled());

        properties.setHost("custom-host");
        assertEquals("custom-host", properties.getHost());

        properties.setSsl(new A2AServerProperties.A2AServerSSLProperties(true));
        assertTrue(properties.getSsl().enabled());

        A2AServerProperties.A2AServerTransportProperties grpcProps =
                new A2AServerProperties.A2AServerTransportProperties(true);
        properties.setGrpc(grpcProps);
        assertTrue(properties.getGrpc().enabled());

        A2AServerProperties.A2AServerTransportProperties jsonrpcProps =
                new A2AServerProperties.A2AServerTransportProperties(true);
        properties.setJsonrpc(jsonrpcProps);
        assertTrue(properties.getJsonrpc().enabled());

        A2AServerProperties.A2AServerTransportProperties restProps =
                new A2AServerProperties.A2AServerTransportProperties(true);
        properties.setRest(restProps);
        assertTrue(properties.getRest().enabled());
    }

    @Test
    void testConstants() {
        assertEquals("spring.ai.a2a.server", A2AServerProperties.CONFIG_PREFIX);
        assertEquals("grpc", A2AServerProperties.CONFIG_GRPC_INFIX);
        assertEquals("jsonrpc", A2AServerProperties.CONFIG_JSON_RPC_INFIX);
        assertEquals("rest", A2AServerProperties.CONFIG_REST_INFIX);
        assertEquals("*", A2AServerProperties.DEFAULT_A2A_SERVER_HOST);
        assertTrue(A2AServerProperties.DEFAULT_A2A_SERVER_ENABLED);
        assertFalse(A2AServerProperties.DEFAULT_CONFIG_GRPC_ENABLED);
        assertFalse(A2AServerProperties.DEFAULT_CONFIG_JSON_RPC_ENABLED);
        assertFalse(A2AServerProperties.DEFAULT_CONFIG_REST_ENABLED);
    }

    @Test
    void testTransportPropertiesDefaultConstructor() {
        A2AServerProperties.A2AServerTransportProperties transportProps =
                new A2AServerProperties.A2AServerTransportProperties();

        assertFalse(transportProps.enabled());
    }

    @Test
    void testTransportPropertiesWithValue() {
        A2AServerProperties.A2AServerTransportProperties transportProps =
                new A2AServerProperties.A2AServerTransportProperties(true);

        assertTrue(transportProps.enabled());
    }

    @Test
    void testSSLPropertiesDefaultConstructor() {
        A2AServerProperties.A2AServerSSLProperties sslProps =
                new A2AServerProperties.A2AServerSSLProperties();

        assertFalse(sslProps.enabled());
    }

    @Test
    void testSSLPropertiesWithValue() {
        A2AServerProperties.A2AServerSSLProperties sslPropsEnabled =
                new A2AServerProperties.A2AServerSSLProperties(true);
        assertTrue(sslPropsEnabled.enabled());

        A2AServerProperties.A2AServerSSLProperties sslPropsDisabled =
                new A2AServerProperties.A2AServerSSLProperties(false);
        assertFalse(sslPropsDisabled.enabled());
    }

    @Test
    void testSSLPropertiesEquality() {
        A2AServerProperties.A2AServerSSLProperties sslProps1 =
                new A2AServerProperties.A2AServerSSLProperties(true);
        A2AServerProperties.A2AServerSSLProperties sslProps2 =
                new A2AServerProperties.A2AServerSSLProperties(true);

        assertEquals(sslProps1, sslProps2);
        assertEquals(sslProps1.hashCode(), sslProps2.hashCode());
    }

    @Test
    void testSetSSLWithNull() {
        A2AServerProperties properties = new A2AServerProperties();
        properties.setSsl(null);

        assertNull(properties.getSsl());
    }
}
