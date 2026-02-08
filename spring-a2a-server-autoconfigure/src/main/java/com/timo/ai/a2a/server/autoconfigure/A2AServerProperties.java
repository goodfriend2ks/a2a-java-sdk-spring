package com.timo.ai.a2a.server.autoconfigure;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Spring AI A2A Server.
 * <p>
 * These properties allow customization of basic A2A server behavior.
 *
 * <p>
 * Example configuration:<pre>
 * spring:
 *   ai:
 *     a2a:
 *       server:
 *         enabled: true
 *         host: "*"
 *         ssl: false
 *         grpc:
 *           enabled: false
 *         jsonrpc:
 *           enabled: false
 *         rest:
 *           enabled: false
 * </pre>
 *
 * @author Timo
 * @since 0.1.0
 */
@ConfigurationProperties(prefix = A2AServerProperties.CONFIG_PREFIX)
public class A2AServerProperties {
    public static final String CONFIG_PREFIX = "spring.ai.a2a.server";
    public static final String CONFIG_GRPC_INFIX = "grpc";
    public static final String CONFIG_JSON_RPC_INFIX = "jsonrpc";
    public static final String CONFIG_REST_INFIX = "rest";

    public static final String DEFAULT_A2A_SERVER_HOST = "*";

    public static final boolean DEFAULT_A2A_SERVER_ENABLED = true;
    public static final boolean DEFAULT_CONFIG_GRPC_ENABLED = false;
    public static final boolean DEFAULT_CONFIG_JSON_RPC_ENABLED = false;
    public static final boolean DEFAULT_CONFIG_REST_ENABLED = false;

    private boolean enabled = DEFAULT_A2A_SERVER_ENABLED;
    private String host = DEFAULT_A2A_SERVER_HOST;
    private @Nullable A2AServerSSLProperties ssl;
    private A2AServerTransportProperties grpc = new A2AServerTransportProperties(DEFAULT_CONFIG_GRPC_ENABLED);
    private A2AServerTransportProperties jsonrpc = new A2AServerTransportProperties(DEFAULT_CONFIG_JSON_RPC_ENABLED);
    private A2AServerTransportProperties rest = new A2AServerTransportProperties(DEFAULT_CONFIG_REST_ENABLED);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public @Nullable A2AServerSSLProperties getSsl() {
        return ssl;
    }

    public void setSsl(@Nullable A2AServerSSLProperties ssl) {
        this.ssl = ssl;
    }

    public A2AServerTransportProperties getGrpc() {
        return grpc;
    }

    public void setGrpc(A2AServerTransportProperties grpc) {
        this.grpc = grpc;
    }

    public A2AServerTransportProperties getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(A2AServerTransportProperties jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public A2AServerTransportProperties getRest() {
        return rest;
    }

    public void setRest(A2AServerTransportProperties rest) {
        this.rest = rest;
    }

    public record A2AServerTransportProperties(boolean enabled) {
        public A2AServerTransportProperties() {
            this(false);
        }
    }

    public record A2AServerSSLProperties(boolean enabled) {
        public A2AServerSSLProperties() {
            this(false);
        }
    }
}
