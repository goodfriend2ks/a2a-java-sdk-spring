package com.timo.ai.a2a.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Documented
public @interface SkillActionParam {
    /**
     * Whether the tool argument is required.
     */
    boolean required() default true;

    /**
     * The description of the tool argument.
     */
    String description() default "";
}
