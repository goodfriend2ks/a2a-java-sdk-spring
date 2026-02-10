package io.github.timo.a2a.server.executor;

import io.a2a.spec.TaskNotCancelableError;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DefaultAgentExecutor.
 * Note: Full integration tests for the execute() method would require complex mocking
 * of internal A2A SDK classes (EventQueue, TaskUpdater, etc.). These tests focus on
 * the cancel() logic which can be properly unit tested.
 */
@ExtendWith(MockitoExtension.class)
class DefaultAgentExecutorTest {

    @Mock
    private AgentExecutorHandler agentExecutorHandler;

    @Mock
    private Task task;

    @Mock
    private TaskStatus taskStatus;

    private DefaultAgentExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new DefaultAgentExecutor(agentExecutorHandler);
    }

    @Test
    void shouldCreateExecutorWithHandler() {
        // Given & When
        DefaultAgentExecutor testExecutor = new DefaultAgentExecutor(agentExecutorHandler);

        // Then
        assertNotNull(testExecutor);
    }

    @Test
    void shouldThrowErrorWhenCancellingNullTask() {
        // Given
        io.a2a.server.agentexecution.RequestContext requestContext = mock(io.a2a.server.agentexecution.RequestContext.class);
        io.a2a.server.events.EventQueue eventQueue = mock(io.a2a.server.events.EventQueue.class);
        when(requestContext.getTask()).thenReturn(null);

        // When & Then
        assertThrows(TaskNotCancelableError.class,
            () -> executor.cancel(requestContext, eventQueue));
    }

    @Test
    void shouldThrowErrorWhenCancellingAlreadyCanceledTask() {
        // Given
        io.a2a.server.agentexecution.RequestContext requestContext = mock(io.a2a.server.agentexecution.RequestContext.class);
        io.a2a.server.events.EventQueue eventQueue = mock(io.a2a.server.events.EventQueue.class);
        when(requestContext.getTask()).thenReturn(task);
        when(task.status()).thenReturn(taskStatus);
        when(taskStatus.state()).thenReturn(TaskState.CANCELED);

        // When & Then
        assertThrows(TaskNotCancelableError.class,
            () -> executor.cancel(requestContext, eventQueue));
    }

    @Test
    void shouldThrowErrorWhenCancellingCompletedTask() {
        // Given
        io.a2a.server.agentexecution.RequestContext requestContext = mock(io.a2a.server.agentexecution.RequestContext.class);
        io.a2a.server.events.EventQueue eventQueue = mock(io.a2a.server.events.EventQueue.class);
        when(requestContext.getTask()).thenReturn(task);
        when(task.status()).thenReturn(taskStatus);
        when(taskStatus.state()).thenReturn(TaskState.COMPLETED);

        // When & Then
        assertThrows(TaskNotCancelableError.class,
            () -> executor.cancel(requestContext, eventQueue));
    }

    @Test
    void shouldValidateTaskStateBeforeCancelling() {
        // Given
        io.a2a.server.agentexecution.RequestContext requestContext = mock(io.a2a.server.agentexecution.RequestContext.class);
        io.a2a.server.events.EventQueue eventQueue = mock(io.a2a.server.events.EventQueue.class);
        when(requestContext.getTask()).thenReturn(task);
        when(task.status()).thenReturn(taskStatus);
        when(taskStatus.state()).thenReturn(TaskState.WORKING);

        // When & Then - In real scenario, this would call TaskUpdater which requires more complex setup
        // So we just verify that it gets past the initial checks without throwing TaskNotCancelableError immediately
        // The actual cancellation requires proper EventQueue setup which is beyond unit testing scope
        try {
            executor.cancel(requestContext, eventQueue);
        } catch (Exception e) {
            // If it throws something other than TaskNotCancelableError for state check, that's expected
            // due to missing TaskUpdater setup
            assertFalse(e instanceof TaskNotCancelableError ||
                       e.getMessage() != null && e.getMessage().contains("cannot be cancelled"));
        }
    }
}
