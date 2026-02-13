package io.github.timo.a2a.spring.ai.agent.example.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("a2a")
data class RemoteAgentConfig(
    val remoteAgents: Map<String, RemoteAgentSetting>
)

data class RemoteAgentSetting(
    val endpoint: String,
    val healthCheckUrl: String? = null,
)

