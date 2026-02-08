package com.timo.ai.a2a.server.executor;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.A2AError;
import io.a2a.spec.DataPart;
import io.a2a.spec.FilePart;
import io.a2a.spec.InternalError;
import io.a2a.spec.TaskNotCancelableError;
import io.a2a.spec.TaskState;
import io.a2a.spec.TextPart;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class DefaultAgentExecutor implements AgentExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAgentExecutor.class);

    private final AgentExecutorHandler agentExecutorHandler;

    public DefaultAgentExecutor(AgentExecutorHandler agentExecutorHandler) {
        this.agentExecutorHandler = agentExecutorHandler;
    }

    @Override
    public void execute(@NonNull RequestContext context, @NonNull EventQueue eventQueue) throws A2AError {
        var updater = new TaskUpdater(context, eventQueue);

        try {
            if (context.getTask() == null) {
                updater.submit();
            }
            updater.startWork();

            // Call user's method with clean string parameter
            var response = this.agentExecutorHandler.execute(context);

            if (LOGGER.isDebugEnabled()) {
                switch (response) {
                    case TextPart textPart -> LOGGER.debug("AI Response text message: {}", textPart.text());
                    case FilePart filePart -> LOGGER.debug("AI Response file: {}, mime: {}",
                            filePart.file().name(),
                            filePart.file().mimeType()
                    );
                    case DataPart dataPart -> LOGGER.debug("AI Response data: {}", dataPart.data());
                    default -> LOGGER.warn("AI Response unknown response: {}", response);
                }
            }

            updater.addArtifact(Collections.singletonList(response), null, null, null);
            updater.complete();
        } catch (A2AError ex) {
            LOGGER.error("JSONRPC error processing task", ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Error executing agent task", ex);
            throw new InternalError("Agent execution failed: " + ex.getMessage());
        }
    }

    @Override
    public void cancel(RequestContext context, @NonNull EventQueue eventQueue) throws A2AError {
        LOGGER.debug("Cancelling task: {}", context.getTaskId());

        var task = context.getTask();
        if (task == null) {
            throw new TaskNotCancelableError();
        }

        if (task.status().state() == TaskState.CANCELED) {
            // task already canceled
            throw new TaskNotCancelableError();
        }

        if (task.status().state() == TaskState.COMPLETED) {
            // task already completed
            throw new TaskNotCancelableError();
        }

        var updater = new TaskUpdater(context, eventQueue);
        updater.cancel();
    }
}
