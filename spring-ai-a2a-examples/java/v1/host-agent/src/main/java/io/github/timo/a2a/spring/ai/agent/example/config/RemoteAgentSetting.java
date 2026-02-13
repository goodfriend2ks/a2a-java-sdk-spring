package io.github.timo.a2a.spring.ai.agent.example.config;

public record RemoteAgentSetting(
    String endpoint,
    String healthCheckUrl
) {
}
