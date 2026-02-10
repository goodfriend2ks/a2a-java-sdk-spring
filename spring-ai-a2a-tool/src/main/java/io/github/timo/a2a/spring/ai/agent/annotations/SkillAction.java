package io.github.timo.a2a.spring.ai.agent.annotations;

import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.execution.ToolCallResultConverter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark the method as an action which can be called by AI
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface SkillAction {

    /**
     * The name of the agent skill action. If not provided, the method name will be used.
     * <p>
     * For maximum compatibility across different LLMs, it is recommended to use only
     * alphanumeric characters, underscores, hyphens, and dots in action names. Using spaces
     * or special characters may cause issues with some LLMs (e.g., OpenAI).
     * </p>
     * <p>
     * Examples of recommended names: "get_weather", "search-docs", "tool.v1"
     * </p>
     * <p>
     * Examples of names that may cause compatibility issues: "get weather" (contains
     * space), "tool()" (contains parentheses)
     * </p>
     */
    String name() default "";

    /**
     * The description of the agent skill action. If not provided, the method name will be used.
     */
    String description() default "";


    /**
     * Whether the skill action result should be returned directly or passed back to the model.
     */
    boolean returnDirect() default false;

    /**
     * The class to use to convert the skill action call result to a String.
     */
    Class<? extends ToolCallResultConverter> resultConverter() default DefaultToolCallResultConverter.class;


    // ActionRisk riskLevel() default ActionRisk.LOW;

    String prompt() default "";
}
