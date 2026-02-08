package com.timo.ai.a2a.server.autoconfigure;

import com.timo.ai.a2a.server.SpringA2AServerProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Spring Boot auto-configuration for A2A web server.
 *
 * @author Timo
 * @since 0.1.0
 */
@AutoConfiguration
public class SpringA2AServerAutoConfiguration {
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SpringA2AServerProperties servletWebServerProperties(Environment environment) {
        return this.buildWebServerProperties(environment, "server.servlet.context-path");
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    SpringA2AServerProperties reactiveWebServerProperties(Environment environment) {
        return this.buildWebServerProperties(environment, "spring.webflux.base-path");
    }

    private SpringA2AServerProperties buildWebServerProperties(Environment environment, String contextPathProperty) {
        var serverHost = this.getHostName("localhost");
        var serverPort = environment.getProperty("server.port", Integer.class, 8080);
        var contextPath = environment.getProperty(contextPathProperty, "").trim();
        var sslEnabled = environment.getProperty("server.ssl.enabled", Boolean.class, false);

        var grpcServerEnabled = environment.getProperty("spring.grpc.server.enabled", Boolean.class, true);
        var grpcServerHost = environment.getProperty("spring.grpc.server.host", "");
        var grpcServerPort = environment.getProperty("spring.grpc.server.port", Integer.class, 9091);

        var a2aServerHost = environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + ".host",
                A2AServerProperties.DEFAULT_A2A_SERVER_HOST
        );
        var a2aServerSSLEnabled = environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + ".ssl.enabled", ""
        ).toLowerCase();

        var a2aServerGrpcEnabled = grpcServerEnabled && environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + "." + A2AServerProperties.CONFIG_GRPC_INFIX + ".enabled",
                Boolean.class,
                A2AServerProperties.DEFAULT_CONFIG_GRPC_ENABLED
        );

        var a2aServerJsonRpcEnabled = environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + "." + A2AServerProperties.CONFIG_JSON_RPC_INFIX + ".enabled",
                Boolean.class,
                A2AServerProperties.DEFAULT_CONFIG_JSON_RPC_ENABLED
        );

        var a2aServerRestEnabled = environment.getProperty(
                A2AServerProperties.CONFIG_PREFIX + "." + A2AServerProperties.CONFIG_REST_INFIX + ".enabled",
                Boolean.class,
                A2AServerProperties.DEFAULT_CONFIG_REST_ENABLED
        );

        var a2aServerHostName = a2aServerHost.isEmpty()
                || A2AServerProperties.DEFAULT_A2A_SERVER_HOST.equals(a2aServerHost)
                ? serverHost
                : a2aServerHost;
        var a2aServerGrpcHostName = grpcServerHost.isEmpty() || "*".equals(grpcServerHost)
                ? a2aServerHostName
                : grpcServerHost;

        return new SpringA2AServerProperties(
                serverHost,
                serverPort,
                contextPath.endsWith("/") ? contextPath.substring(0, contextPath.length() - 1) : contextPath,
                sslEnabled,
                a2aServerHostName,
                a2aServerGrpcHostName,
                grpcServerPort,
                "true".equals(a2aServerSSLEnabled) || !("false".equals(a2aServerSSLEnabled) || !sslEnabled),
                a2aServerGrpcEnabled,
                a2aServerJsonRpcEnabled,
                a2aServerRestEnabled
        );
    }

    private String getHostName(String defaultValue) {
        try {
            var hostName = InetAddress.getLocalHost().getHostName();
            if (hostName == null || hostName.isEmpty()) {
                return defaultValue;
            }

            return hostName;
        } catch (UnknownHostException e) {
            // N/A
        }

        return defaultValue;
    }
}
