package com.timo.ai.a2a.server.autoconfigure;

import io.a2a.server.config.A2AConfigProvider;
import io.a2a.server.config.DefaultValuesConfigProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.Environment;

import java.util.Optional;

/**
 * Spring Environment based A2A configuration provider. It first checks the Spring
 * {@link Environment} for the property. If not found, it falls back to
 * {@link  DefaultValuesConfigProvider}.
 *
 * <p>
 * This allows overriding default A2A server properties using standard Spring {@link Environment}
 * properties.
 *
 * @author Timo
 * @since 0.1.0
 */
public class SpringA2AConfigProvider implements A2AConfigProvider {
    public static final String A2A_BLOCKING_AGENT_TIMEOUT_SECONDS = "a2a.blocking.agent.timeout.seconds";
    public static final String A2A_BLOCKING_CONSUMPTION_TIMEOUT_SECONDS = "a2a.blocking.consumption.timeout.seconds";

    public static final String A2A_EXECUTOR_CORE_POOL_SIZE = "a2a.executor.core-pool-size";
    public static final String A2A_EXECUTOR_MAX_POOL_SIZE = "a2a.executor.max-pool-size";
    public static final String A2A_EXECUTOR_KEEP_ALIVE_SECONDS = "a2a.executor.keep-alive-seconds";

    public static final int DEFAULT_A2A_EXECUTOR_CORE_POOL_SIZE = 1;
    public static final int DEFAULT_A2A_EXECUTOR_MAX_POOL_SIZE = 10;
    public static final long DEFAULT_A2A_EXECUTOR_KEEP_ALIVE_SECONDS = 60L;

    private final Environment environment;
    private final DefaultValuesConfigProvider defaultValues;

    public SpringA2AConfigProvider(Environment environment, DefaultValuesConfigProvider defaultValues) {
        this.environment = environment;
        this.defaultValues = defaultValues;
    }

    @Override
    public @Nullable String getValue(@NonNull String name) {
        if (environment.containsProperty(name)) {
            return environment.getProperty(name);
        }

        // Fallback to defaults
        return defaultValues.getValue(name);
    }

    @Override
    public @NullMarked Optional<String> getOptionalValue(String name) {
        if (environment.containsProperty(name)) {
            return Optional.ofNullable(environment.getProperty(name));
        }

        // Fallback to defaults
        return defaultValues.getOptionalValue(name);
    }
}
