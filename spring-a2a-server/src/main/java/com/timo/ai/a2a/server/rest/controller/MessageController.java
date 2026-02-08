package com.timo.ai.a2a.server.rest.controller;

import com.timo.ai.a2a.server.context.CallContextFactory;
import com.timo.ai.a2a.server.rest.ResponseUtils;
import com.timo.ai.a2a.server.rest.handler.AgentRestHandler;
import io.a2a.spec.A2AMethods;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

/**
 * REST controller for A2A message sending.
 *
 * @author Timo
 * @since 0.1.0
 */
@RestController
@RequestMapping("/messages")
public class MessageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    private static final String CONTEXT_ID_PARAM = "contextId";
    private static final String TASK_ID_PARAM = "taskId";

    private final AgentRestHandler agentRestHandler;
    private final CallContextFactory callContextFactory;

    public MessageController(AgentRestHandler agentRestHandler, CallContextFactory callContextFactory) {
        this.agentRestHandler = agentRestHandler;
        this.callContextFactory = callContextFactory;
    }

    /**
     * Handles sendMessage JSON-RPC requests.
     */
    @PostMapping(
            path = "/message:send",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> sendMessage(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @RequestBody String jsonRpcMessage
    ) {
        LOGGER.debug("Received request to send agent message: {}", jsonRpcMessage);

        var context = callContextFactory.build(exchange, authentication, A2AMethods.SEND_MESSAGE_METHOD);
        var response = agentRestHandler.sendMessage(jsonRpcMessage, context.getTenantUid(), context);

        return ResponseUtils.toResponseEntity(response);
    }

    /**
     * Handles send user text message requests.
     */
    @PostMapping(
            path = {"/user-message", "/user-message:send"},
            consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> sendTextMessage(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @Nullable @RequestParam(value = CONTEXT_ID_PARAM, required = false) String contextId,
            @Nullable @RequestParam(value = TASK_ID_PARAM, required = false) String taskId,
            @RequestBody String userMessage
    ) {
        LOGGER.debug("Received request to send user message: {}", userMessage);

        var context = callContextFactory.build(exchange, authentication, A2AMethods.SEND_MESSAGE_METHOD);
        var response = agentRestHandler.sendUserMessage(
                userMessage, context.getTenantUid(),
                context, contextId, taskId
        );
        return ResponseUtils.toResponseEntity(response);
    }

    /**
     * Handles sendMessage JSON-RPC requests in streaming.
     */
    @PostMapping(
            path = "/message:stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public ResponseEntity<?> sendMessageStreaming(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @RequestBody String jsonRpcMessage
    ) {
        LOGGER.debug("Received request to send agent message in stream: {}", jsonRpcMessage);

        var context = callContextFactory.build(exchange, authentication, A2AMethods.SEND_STREAMING_MESSAGE_METHOD);
        var response = agentRestHandler.sendStreamingMessage(jsonRpcMessage, context.getTenantUid(), context);
        return ResponseUtils.toResponseEntity(response);
    }

    /**
     * Handles send user text message requests in streaming.
     */
    @PostMapping(
            path = {"/user-message/stream", "/user-message:stream"},
            consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE },
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public ResponseEntity<?> sendTextMessageStreaming(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @Nullable @RequestParam(value = CONTEXT_ID_PARAM, required = false) String contextId,
            @Nullable @RequestParam(value = TASK_ID_PARAM, required = false) String taskId,
            @RequestBody String userMessage
    ) {
        LOGGER.debug("Received request to send user message in stream: {}", userMessage);

        var context = callContextFactory.build(exchange, authentication, A2AMethods.SEND_STREAMING_MESSAGE_METHOD);
        var response = agentRestHandler.sendStreamingUserMessage(
                userMessage, context.getTenantUid(),
                context, contextId, taskId
        );

        return ResponseUtils.toResponseEntity(response);
    }
}
