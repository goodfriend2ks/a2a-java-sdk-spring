package com.timo.ai.a2a.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import io.a2a.spec.AgentSkill;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AgentSkillResponse {
    private final String id;
    private final String name;
    private final String description;
    private final List<String> tags;
    private final List<String> examples;
    private final List<String> inputModes;
    private final List<String> outputModes;

    @JsonIgnore
    private final List<Map<String, List<String>>> security;

    private List<SkillActionResponse> actions;

    public AgentSkillResponse(AgentSkill agentSkill) {
        this.id = agentSkill.id();
        this.name = agentSkill.name();
        this.description = agentSkill.description();
        this.tags = CollectionUtils.isEmpty(agentSkill.tags())
                ? Collections.emptyList()
                : Collections.unmodifiableList(agentSkill.tags());
        this.examples = CollectionUtils.isEmpty(agentSkill.examples())
                ? Collections.emptyList()
                : Collections.unmodifiableList(agentSkill.examples());
        this.inputModes = CollectionUtils.isEmpty(agentSkill.inputModes())
                ? Collections.emptyList()
                : Collections.unmodifiableList(agentSkill.inputModes());
        this.outputModes = CollectionUtils.isEmpty(agentSkill.outputModes())
                ? Collections.emptyList()
                : Collections.unmodifiableList(agentSkill.outputModes());
        this.security = CollectionUtils.isEmpty(agentSkill.security())
                ? Collections.emptyList()
                : Collections.unmodifiableList(agentSkill.security());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getExamples() {
        return examples;
    }

    public List<String> getInputModes() {
        return inputModes;
    }

    public List<String> getOutputModes() {
        return outputModes;
    }

    public List<Map<String, List<String>>> getSecurity() {
        return security;
    }

    public List<SkillActionResponse> getActions() {
        return actions;
    }

    public void setActions(List<SkillActionResponse> actions) {
        this.actions = actions;
    }

    public static record SkillActionResponse(String name, String description, JsonNode inputSchema) {
    }
}
