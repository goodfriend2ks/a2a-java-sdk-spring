package io.github.timo.a2a.spring.ai.agent.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("a2a")
public record RemoteAgentConfig(
    Map<String, RemoteAgentSetting> remoteAgents
) {
}
