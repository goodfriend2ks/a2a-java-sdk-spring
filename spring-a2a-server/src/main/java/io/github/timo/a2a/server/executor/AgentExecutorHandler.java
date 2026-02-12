package io.github.timo.a2a.server.executor;

import io.a2a.server.agentexecution.RequestContext;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.TextPart;

/**
 * Executes AI operations with A2A RequestContext for A2A agents.
 * <p>
 * This interface is used internally by {@link DefaultAgentExecutor} for executing
 * AI operations in response to A2A protocol requests.
 *
 * @author Timo
 * @since 0.1.0
 */
@FunctionalInterface
public interface AgentExecutorHandler {
    /**
     * Execute and return response.
     * @param requestContext the A2A RequestContext containing message, task, and context IDs
     * @return the response result
     */
    Part<?> execute(RequestContext requestContext);

    /**
     * The util function to extract text content from A2A message.
     * @param message the A2A messages
     * @return the content from A2A text message ({@link  TextPart})
     */
    static String extractTextFromMessage(Message message) {
        var messageBuilder = new StringBuilder();
        for (Part<?> part : message.parts()) {
            if (part instanceof TextPart(String text)) {
                messageBuilder.append(text);
            }
        }

        return messageBuilder.toString().trim();
    }
}
