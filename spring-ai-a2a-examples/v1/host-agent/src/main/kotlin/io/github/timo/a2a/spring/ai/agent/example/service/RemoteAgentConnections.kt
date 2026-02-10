package io.github.timo.a2a.spring.ai.agent.example.service

import io.github.timo.a2a.spring.ai.agent.example.config.RemoteAgentConfig
import io.a2a.A2A
import io.a2a.spec.AgentCard
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI

@Service
class RemoteAgentConnections(
    private val remoteAgentConfig: RemoteAgentConfig
) {
    private val logger = LoggerFactory.getLogger(RemoteAgentConnections::class.java)

    final var remoteAgentCards: Map<String, AgentCard>

    init {
        logger.info("Initializing remote agent connections")

        remoteAgentCards = remoteAgentConfig.remoteAgents.mapNotNull { (agentName, remoteAgentConfig) ->
            try {
                val url = remoteAgentConfig.endpoint
                logger.info("Connecting remote agent: {} at {}", agentName, url)
                val path = URI(url).path.removeSuffix("/").takeIf { it.isNotBlank() }?.let { "$it/" } ?: ""
                val card = A2A.getAgentCard(url, "$path.well-known/agent-card.json", null)
                logger.info("Discovered agent: {} at {}", card.name(), url);

                agentName to card
            } catch (@Suppress("TooGenericExceptionCaught") ex: Exception) {
                logger.error("Problem connecting remote agent connection", ex)
                null
            }
        }.toMap()
    }

    /**
     * Returns a JSON-formatted description of all available agents for the system prompt.
     */
    fun getAgentDescriptions(): String {
        return this.remoteAgentCards.values.joinToString("\n") {
            "{\"name\": \"${it.name}\", \"description\": \"${it.description ?: "No description"}\"}"
        }
    }

    /**
     * Returns the list of available agent names.
     */
    fun getAgentNames(): List<String> {
        return this.remoteAgentCards.keys.toList()
    }
}
