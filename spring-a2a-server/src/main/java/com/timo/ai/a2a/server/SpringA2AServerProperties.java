package com.timo.ai.a2a.server;

import io.a2a.spec.AgentInterface;
import io.a2a.spec.TransportProtocol;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public record SpringA2AServerProperties(
        String serverHost,
        int serverPort,
        String contextPath,
        boolean sslEnabled,

        String a2aServerHost,
        String a2aServerGrpcHost,
        Integer a2aServerGrpcPort,

        boolean a2aServerSSLEnabled,
        boolean a2aServerGrpcEnabled,
        boolean a2aServerJsonRpcEnabled,
        boolean a2aServerRestEnabled
) {
    public List<AgentInterface> buildAgentInterfaces() {
        return buildAgentInterfaces("", "messages");
    }

    public List<AgentInterface> buildAgentInterfaces(
            String jsonRpcPath,
            String restPath
    ) {
        var a2aServerSchema = a2aServerSSLEnabled ? "https" : "http";
        var a2aServerContextPath = contextPath.endsWith("/")
                ? contextPath.substring(0, contextPath.length() - 1)
                : contextPath;
        var httpServerPath = a2aServerSchema + "://" + a2aServerHost + ":" + serverPort
                + a2aServerContextPath + "/";

        var supportedInterfaces = new AgentInterface[]{
                a2aServerGrpcEnabled ? new AgentInterface(
                        TransportProtocol.GRPC.asString(),
                        a2aServerGrpcHost + ":" + a2aServerGrpcPort
                ) : null,

                a2aServerJsonRpcEnabled ?
                        new AgentInterface(
                                TransportProtocol.JSONRPC.asString(),
                                httpServerPath + Optional.ofNullable(jsonRpcPath).orElse("")
                        ) : null,

                a2aServerRestEnabled ?
                        new AgentInterface(
                                TransportProtocol.HTTP_JSON.asString(),
                                httpServerPath + Optional.ofNullable(restPath).orElse("")
                        ) : null
        };

        return Stream.of(supportedInterfaces)
                .filter(Objects::nonNull)
                .toList();
    }
}
