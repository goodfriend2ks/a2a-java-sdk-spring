package com.timo.ai.a2a.processor;

import com.timo.ai.a2a.annotations.SkillAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.DefaultToolMetadata;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AgentActionCallbackProvider implements ToolCallbackProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentActionCallbackProvider.class);

    private final Map<String, ToolCallback[]> agentActionCallbacks;
    private final ToolCallback[] agentToolCallbacks;

    AgentActionCallbackProvider(Map<String, Object> agentSkillBeans) {
        this.agentActionCallbacks = Collections.unmodifiableMap(
                this.buildAgentActionCallbacks(agentSkillBeans)
        );

        this.agentToolCallbacks = this.agentActionCallbacks.values().stream()
                .flatMap(Stream::of)
                .toArray(ToolCallback[]::new);

        validateToolCallbacks(agentSkillBeans, this.agentToolCallbacks);
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return agentToolCallbacks;
    }

    public Map<String, ToolCallback[]> getAgentActionCallbacks() {
        return agentActionCallbacks;
    }

    private Map<String, ToolCallback[]> buildAgentActionCallbacks(Map<String, Object> agentSkillBeans) {
        Map<String, ToolCallback[]> cachedAgentSkillToolCallbacks = new HashMap<>(agentSkillBeans.size(), 1.0F);

        for (var agentSkillEntry : agentSkillBeans.entrySet()) {
            var skillBean = agentSkillEntry.getValue();
            var toolCallBacks = getSkillActionMethods(skillBean)
                    .map(skillActionMethod -> MethodToolCallback.builder()
                            .toolDefinition(AgentSkillActionDefinitions.from(skillActionMethod))
                            .toolMetadata(AgentSkillActionMetadata.from(skillActionMethod))
                            .toolMethod(skillActionMethod)
                            .toolObject(skillBean)
                            .toolCallResultConverter(
                                    SkillActionUtils.getSkillActionCallResultConverter(skillActionMethod)
                            )
                            .build())
                    .toArray(ToolCallback[]::new);
            cachedAgentSkillToolCallbacks.put(agentSkillEntry.getKey(), toolCallBacks);
        }

        return cachedAgentSkillToolCallbacks;
    }

    private Stream<Method> getSkillActionMethods(Object skillBean) {
        return Stream.of(ReflectionUtils.getDeclaredMethods(
                                AopUtils.isAopProxy(skillBean)
                                        ? AopUtils.getTargetClass(skillBean)
                                        : skillBean.getClass()
                        )
                ).filter(this::isSkillActionAnnotatedMethod)
                .filter(skillActionMethod -> !isFunctionalType(skillActionMethod))
                .filter(ReflectionUtils.USER_DECLARED_METHODS::matches);
    }

    private boolean isFunctionalType(Method skillActionMethod) {
        var isFunction = ClassUtils.isAssignable(Function.class, skillActionMethod.getReturnType())
                || ClassUtils.isAssignable(Supplier.class, skillActionMethod.getReturnType())
                || ClassUtils.isAssignable(Consumer.class, skillActionMethod.getReturnType());

        if (isFunction) {
            LOGGER.warn("Method {} is annotated with @SkillAction but returns a functional type. "
                    + "This is not supported and the method will be ignored.", skillActionMethod.getName());
        }

        return isFunction;
    }

    private boolean isSkillActionAnnotatedMethod(Method method) {
        SkillAction annotation = AnnotationUtils.findAnnotation(method, SkillAction.class);
        return Objects.nonNull(annotation);
    }

    private void validateToolCallbacks(Map<String, Object> agentSkillBeans, ToolCallback[] toolCallbacks) {
        List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
        if (!duplicateToolNames.isEmpty()) {
            throw new IllegalStateException("Multiple skill actions with the same name (%s) found in sources: %s"
                    .formatted(
                            String.join(", ", duplicateToolNames),
                            agentSkillBeans.values().stream()
                                    .map(o -> o.getClass().getName())
                                    .collect(Collectors.joining(", ")))
            );
        }
    }

    private static class AgentSkillActionDefinitions {
        public static DefaultToolDefinition.Builder builder(Method method) {
            Assert.notNull(method, "method cannot be null");

            return DefaultToolDefinition.builder()
                    .name(SkillActionUtils.getSkillActionName(method))
                    .description(SkillActionUtils.getSkillActionDescription(method))
                    .inputSchema(SkillActionJsonSchemaGenerator.generateForMethodInput(method));
        }

        /**
         * Create a default {@link ToolDefinition} instance from a {@link Method}.
         */
        public static ToolDefinition from(Method method) {
            return builder(method).build();
        }
    }

    private static class AgentSkillActionMetadata {
        /**
         * Create a default {@link ToolMetadata} builder.
         */
        static DefaultToolMetadata.Builder builder() {
            return DefaultToolMetadata.builder();
        }

        /**
         * Create a default {@link ToolMetadata} instance from a {@link Method}.
         */
        static ToolMetadata from(Method method) {
            Assert.notNull(method, "method cannot be null");
            return builder()
                    .returnDirect(SkillActionUtils.getSkillActionReturnDirect(method))
                    .build();
        }
    }
}
