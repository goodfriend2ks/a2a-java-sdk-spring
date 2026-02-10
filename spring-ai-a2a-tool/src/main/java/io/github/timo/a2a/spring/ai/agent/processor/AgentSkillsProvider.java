package io.github.timo.a2a.spring.ai.agent.processor;

import io.github.timo.a2a.spring.ai.agent.annotations.AgentSkill;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AgentSkillsProvider {
    private final ApplicationContext applicationContext;

    private Map<String, Object> agentSkillBeans;
    private List<io.a2a.spec.AgentSkill> agentSkills;
    private AgentActionCallbackProvider agentActionCallbackProvider;

    public AgentSkillsProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public synchronized List<io.a2a.spec.AgentSkill> getAgentSkills() {
        if (agentSkills == null) {
            synchronized (this) {
                var skillBeans = getAgentSkillBeans();
                var cachedAgentSkills = new ArrayList<io.a2a.spec.AgentSkill>(skillBeans.size());

                for (var skillBeanEntry : skillBeans.entrySet()) {
                    var agentSkillInfo = skillBeanEntry.getValue().getClass().getAnnotation(AgentSkill.class);
                    cachedAgentSkills.add(
                            this.buildAgentSkill(skillBeanEntry.getKey(), agentSkillInfo)
                    );
                }

                agentSkills = Collections.unmodifiableList(cachedAgentSkills);
            }
        }

        return agentSkills;
    }

    public synchronized AgentActionCallbackProvider getAgentActionCallbackProvider() {
        if (agentActionCallbackProvider == null) {
            synchronized (this) {
                var skillBeans = getAgentSkillBeans();
                agentActionCallbackProvider = new AgentActionCallbackProvider(skillBeans);
            }
        }

        return agentActionCallbackProvider;
    }

    public synchronized String buildAgentDescription() {
        var cachedAgentSkills = getAgentSkills();
        var agentActionCallbacks = getAgentActionCallbackProvider().getAgentActionCallbacks();
        var descriptionBuilder = new StringBuilder();

        for (var agentSkill : cachedAgentSkills) {
            if (!descriptionBuilder.isEmpty()) {
                descriptionBuilder.append("; ");
            }

            var actionNames = Arrays.stream(agentActionCallbacks.getOrDefault(agentSkill.id(), new ToolCallback[]{}))
                    .map(toolCallback -> toolCallback.getToolDefinition().name())
                    .collect(Collectors.joining(", "));

            descriptionBuilder.append(agentSkill.name())
                    .append(" (")
                    .append(agentSkill.description())
                    .append("), with actions: ")
                    .append(actionNames);
        }

        if (!descriptionBuilder.isEmpty()) {
            descriptionBuilder.insert(0, "This agent provides the following capabilities: ");
        }

        return descriptionBuilder.toString();
    }

    private synchronized Map<String, Object> getAgentSkillBeans() {
        if (agentSkillBeans == null) {
            agentSkillBeans = new ConcurrentHashMap<>();
        }

        synchronized (this) {
            var agentSkillBeanNames = this.applicationContext.getBeanNamesForAnnotation(AgentSkill.class);
            for (String beanName : agentSkillBeanNames) {
                agentSkillBeans.put(beanName, this.applicationContext.getBean(beanName));
            }
        }

        return agentSkillBeans;
    }

    private io.a2a.spec.AgentSkill buildAgentSkill(String agentSkillId, AgentSkill agentSkillInfo) {
        return io.a2a.spec.AgentSkill.builder()
                .id(agentSkillId)
                .name(agentSkillInfo.name())
                .description(agentSkillInfo.description())
                .inputModes(List.of(agentSkillInfo.inputModes()))
                .outputModes(List.of(agentSkillInfo.outputModes()))
                .tags(List.of(agentSkillInfo.tags()))
                .examples(List.of(agentSkillInfo.examples()))
                .build();
    }
}
