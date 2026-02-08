package com.timo.ai.a2a.server.spring.ai.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = A2AAgentSkillProperties.CONFIG_PREFIX)
public class A2AAgentSkillProperties {
    public static final String CONFIG_PREFIX = "spring.ai.a2a.agent.skill";
    public static final String CONFIG_REST_INFIX = "rest";

    public static final boolean DEFAULT_A2A_AGENT_SKILL_ENABLED = true;
    public static final boolean DEFAULT_A2A_AGENT_SKILL_REST_ENABLED = false;

    private boolean enabled = DEFAULT_A2A_AGENT_SKILL_ENABLED;
    private A2AAgentSkillTransportProperties rest = new A2AAgentSkillTransportProperties(DEFAULT_A2A_AGENT_SKILL_REST_ENABLED);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public A2AAgentSkillTransportProperties getRest() {
        return rest;
    }

    public void setRest(A2AAgentSkillTransportProperties rest) {
        this.rest = rest;
    }

    public record A2AAgentSkillTransportProperties(boolean enabled) {
        public A2AAgentSkillTransportProperties() {
            this(false);
        }
    }
}
