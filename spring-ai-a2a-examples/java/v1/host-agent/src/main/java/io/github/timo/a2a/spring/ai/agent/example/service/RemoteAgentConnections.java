package io.github.timo.a2a.spring.ai.agent.example.service;

import io.github.timo.a2a.spring.ai.agent.example.config.RemoteAgentConfig;
import io.a2a.A2A;
import io.a2a.spec.AgentCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RemoteAgentConnections {

    private final Logger logger = LoggerFactory.getLogger(RemoteAgentConnections.class);
    private final Map<String, AgentCard> remoteAgentCards;

    public RemoteAgentConnections(RemoteAgentConfig remoteAgentConfig) {
        logger.info("Initializing remote agent connections");

        this.remoteAgentCards = new HashMap<>();

        remoteAgentConfig.remoteAgents().forEach((agentName, remoteAgentSetting) -> {
            try {
                String url = remoteAgentSetting.endpoint();
                logger.info("Connecting remote agent: {} at {}", agentName, url);

                String path = URI.create(url).getPath();
                path = path.endsWith("/") ? path : path + "/";
                if (path.equals("/")) {
                    path = "";
                }

                AgentCard card = A2A.getAgentCard(url, path + ".well-known/agent-card.json", null);
                logger.info("Discovered agent: {} at {}", card.name(), url);

                remoteAgentCards.put(agentName, card);
            } catch (Exception ex) {
                logger.error("Problem connecting remote agent connection", ex);
            }
        });
    }

    /**
     * Returns a JSON-formatted description of all available agents for the system prompt.
     */
    public String getAgentDescriptions() {
        return remoteAgentCards.values().stream()
            .map(card -> String.format("{\"name\": \"%s\", \"description\": \"%s\"}",
                card.name(),
                card.description() != null ? card.description() : "No description"))
            .collect(Collectors.joining("\n"));
    }

    /**
     * Returns the list of available agent names.
     */
    public List<String> getAgentNames() {
        return List.copyOf(remoteAgentCards.keySet());
    }

    public Map<String, AgentCard> getRemoteAgentCards() {
        return remoteAgentCards;
    }
}
