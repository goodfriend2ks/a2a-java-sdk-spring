package com.timo.ai.a2a.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark the class as an Agent skill
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
@Inherited
public @interface AgentSkill {

    /**
     * The value may indicate a suggestion for a logical agent skill component name,
     * to be turned into a Spring bean name in case of an autodetected component.
     * @return the suggested agent skill component name, if any (or empty String otherwise)
     */
    @AliasFor(value = "value", annotation = Component.class)
    String id() default "";

    String name();
    String description();
    String[] tags() default {};
    String[] examples () default {};
    String[] inputModes() default {
        "text", "json"
    };
    String[] outputModes() default {
        "text", "json"
    };
    String prompt() default "";
}
