package com.timo.ai.a2a.processor;

import com.timo.ai.a2a.annotations.SkillAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.execution.ToolCallResultConverter;
import org.springframework.ai.util.ParsingUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

final class SkillActionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkillActionUtils.class);

    /**
     * Regular expression pattern for recommended agent skill action names. Agent skill action names should contain
     * only alphanumeric characters, underscores, hyphens, and dots for maximum compatibility across different LLMs.
     */
    private static final Pattern RECOMMENDED_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\.-]+$");

    private SkillActionUtils() {
    }

    public static String getSkillActionName(Method method) {
        Assert.notNull(method, "method cannot be null");
        var skillAction = AnnotatedElementUtils.findMergedAnnotation(method, SkillAction.class);
        var skillActionName = skillAction == null
                ? method.getName()
                : StringUtils.hasText(skillAction.name()) ? skillAction.name() : method.getName();

        validateSkillActionName(skillActionName);
        return skillActionName;
    }

    public static String getSkillActionDescription(Method method) {
        Assert.notNull(method, "method cannot be null");
        var skillAction = AnnotatedElementUtils.findMergedAnnotation(method, SkillAction.class);
        if (skillAction == null) {
            return ParsingUtils.reConcatenateCamelCase(method.getName(), " ");
        }

        return StringUtils.hasText(skillAction.description()) ? skillAction.description() : method.getName();
    }

    public static boolean getSkillActionReturnDirect(Method method) {
        Assert.notNull(method, "method cannot be null");
        var skillAction = AnnotatedElementUtils.findMergedAnnotation(method, SkillAction.class);
        return skillAction != null && skillAction.returnDirect();
    }

    public static ToolCallResultConverter getSkillActionCallResultConverter(Method method) {
        Assert.notNull(method, "method cannot be null");
        var skillAction = AnnotatedElementUtils.findMergedAnnotation(method, SkillAction.class);
        if (skillAction == null) {
            return new DefaultToolCallResultConverter();
        }

        var type = skillAction.resultConverter();
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to instantiate ToolCallResultConverter: " + type, e);
        }
    }

    private static void validateSkillActionName(String skillActionName) {
        Assert.hasText(skillActionName, "Agent skill action name cannot be null or empty");
        if (!RECOMMENDED_NAME_PATTERN.matcher(skillActionName).matches()) {
            LOGGER.warn("Agent skill action name name '{}' may not be compatible with some LLMs (e.g., OpenAI). "
                    + "Consider using only alphanumeric characters, underscores, hyphens, and dots.", skillActionName);
        }
    }
}
