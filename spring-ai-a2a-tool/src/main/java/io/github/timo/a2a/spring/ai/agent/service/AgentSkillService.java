package io.github.timo.a2a.spring.ai.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.timo.a2a.spring.ai.agent.rest.AgentSkillResponse;
import io.github.timo.a2a.spring.ai.agent.processor.AgentSkillsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AgentSkillService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentSkillService.class);

    private final AgentSkillsProvider agentSkillsProvider;
    private final ObjectMapper objectMapper;

    public AgentSkillService(
            AgentSkillsProvider agentSkillsProvider,
            ObjectMapper objectMapper
    ) {
        this.agentSkillsProvider = agentSkillsProvider;
        this.objectMapper = objectMapper;
    }

    public List<AgentSkillResponse> getAgentSkills() {
        var agentSkillToolCallbacks = this.agentSkillsProvider
                .getAgentActionCallbackProvider()
                .getAgentActionCallbacks();

        return this.agentSkillsProvider.getAgentSkills().stream()
                .map(agentSkill -> {
                    var actionResponses = Arrays.stream(
                                    agentSkillToolCallbacks.getOrDefault(agentSkill.id(), new ToolCallback[]{})
                            )
                            .map(toolCallback -> new AgentSkillResponse.SkillActionResponse(
                                    toolCallback.getToolDefinition().name(),
                                    toolCallback.getToolDefinition().description(),
                                    this.parseJsonNode(toolCallback.getToolDefinition().inputSchema())
                            ))
                            .toList();

                    var response = new AgentSkillResponse(agentSkill);
                    response.setActions(actionResponses);
                    return response;
                })
                .toList();
    }

    public String executeSkillAction(String skillId, String actionName, String parameters) throws Exception {
        var agentSkillToolCallbacks = this.agentSkillsProvider
                .getAgentActionCallbackProvider()
                .getAgentActionCallbacks();

        var agentActionCallback = Arrays.stream(agentSkillToolCallbacks.getOrDefault(skillId, new ToolCallback[]{}))
                .filter(toolCallback -> toolCallback.getToolDefinition().name().equals(actionName))
                .findFirst();

        if (agentActionCallback.isEmpty()) {
            throw new AgentActionNotFoundException("Agent skill action " + actionName + " not found");
        }

        return agentActionCallback.get().call(parameters);
    }

    private JsonNode parseJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            LOGGER.error("Error parsing JSON object to JSON", ex);
        }

        return objectMapper.nullNode();
    }

    public static class AgentActionNotFoundException extends RuntimeException {
        public AgentActionNotFoundException(String message) {
            super(message);
        }
    }
}
