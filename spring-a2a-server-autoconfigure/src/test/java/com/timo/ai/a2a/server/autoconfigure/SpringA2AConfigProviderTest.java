package com.timo.ai.a2a.server.autoconfigure;

import io.a2a.server.config.DefaultValuesConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringA2AConfigProviderTest {

    @Mock
    private Environment environment;

    @Mock
    private DefaultValuesConfigProvider defaultValues;

    private SpringA2AConfigProvider configProvider;

    @BeforeEach
    void setUp() {
        configProvider = new SpringA2AConfigProvider(environment, defaultValues);
    }

    @Test
    void testGetValueFromEnvironment() {
        String propertyName = "test.property";
        String expectedValue = "test-value";

        when(environment.containsProperty(propertyName)).thenReturn(true);
        when(environment.getProperty(propertyName)).thenReturn(expectedValue);

        String actualValue = configProvider.getValue(propertyName);

        assertEquals(expectedValue, actualValue);
        verify(environment).containsProperty(propertyName);
        verify(environment).getProperty(propertyName);
        verify(defaultValues, never()).getValue(propertyName);
    }

    @Test
    void testGetValueFallbackToDefault() {
        String propertyName = "test.property";
        String defaultValue = "default-value";

        when(environment.containsProperty(propertyName)).thenReturn(false);
        when(defaultValues.getValue(propertyName)).thenReturn(defaultValue);

        String actualValue = configProvider.getValue(propertyName);

        assertEquals(defaultValue, actualValue);
        verify(environment).containsProperty(propertyName);
        verify(environment, never()).getProperty(propertyName);
        verify(defaultValues).getValue(propertyName);
    }

    @Test
    void testGetOptionalValueFromEnvironment() {
        String propertyName = "test.property";
        String expectedValue = "test-value";

        when(environment.containsProperty(propertyName)).thenReturn(true);
        when(environment.getProperty(propertyName)).thenReturn(expectedValue);

        Optional<String> actualValue = configProvider.getOptionalValue(propertyName);

        assertTrue(actualValue.isPresent());
        assertEquals(expectedValue, actualValue.get());
        verify(environment).containsProperty(propertyName);
        verify(environment).getProperty(propertyName);
        verify(defaultValues, never()).getOptionalValue(propertyName);
    }

    @Test
    void testGetOptionalValueFallbackToDefault() {
        String propertyName = "test.property";
        String defaultValue = "default-value";

        when(environment.containsProperty(propertyName)).thenReturn(false);
        when(defaultValues.getOptionalValue(propertyName)).thenReturn(Optional.of(defaultValue));

        Optional<String> actualValue = configProvider.getOptionalValue(propertyName);

        assertTrue(actualValue.isPresent());
        assertEquals(defaultValue, actualValue.get());
        verify(environment).containsProperty(propertyName);
        verify(environment, never()).getProperty(propertyName);
        verify(defaultValues).getOptionalValue(propertyName);
    }

    @Test
    void testGetOptionalValueEmpty() {
        String propertyName = "test.property";

        when(environment.containsProperty(propertyName)).thenReturn(false);
        when(defaultValues.getOptionalValue(propertyName)).thenReturn(Optional.empty());

        Optional<String> actualValue = configProvider.getOptionalValue(propertyName);

        assertFalse(actualValue.isPresent());
        verify(defaultValues).getOptionalValue(propertyName);
    }

    @Test
    void testConstants() {
        assertEquals("a2a.blocking.agent.timeout.seconds", SpringA2AConfigProvider.A2A_BLOCKING_AGENT_TIMEOUT_SECONDS);
        assertEquals("a2a.blocking.consumption.timeout.seconds", SpringA2AConfigProvider.A2A_BLOCKING_CONSUMPTION_TIMEOUT_SECONDS);
        assertEquals("a2a.executor.core-pool-size", SpringA2AConfigProvider.A2A_EXECUTOR_CORE_POOL_SIZE);
        assertEquals("a2a.executor.max-pool-size", SpringA2AConfigProvider.A2A_EXECUTOR_MAX_POOL_SIZE);
        assertEquals("a2a.executor.keep-alive-seconds", SpringA2AConfigProvider.A2A_EXECUTOR_KEEP_ALIVE_SECONDS);
        assertEquals(1, SpringA2AConfigProvider.DEFAULT_A2A_EXECUTOR_CORE_POOL_SIZE);
        assertEquals(10, SpringA2AConfigProvider.DEFAULT_A2A_EXECUTOR_MAX_POOL_SIZE);
        assertEquals(60L, SpringA2AConfigProvider.DEFAULT_A2A_EXECUTOR_KEEP_ALIVE_SECONDS);
    }
}
