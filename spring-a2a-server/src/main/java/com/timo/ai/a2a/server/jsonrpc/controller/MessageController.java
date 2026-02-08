package com.timo.ai.a2a.server.jsonrpc.controller;

import com.google.gson.JsonSyntaxException;
import com.timo.ai.a2a.server.context.CallContextFactory;
import io.a2a.grpc.utils.JSONRPCUtils;
import io.a2a.grpc.utils.ProtoUtils;
import io.a2a.jsonrpc.common.json.IdJsonMappingException;
import io.a2a.jsonrpc.common.json.InvalidParamsJsonMappingException;
import io.a2a.jsonrpc.common.json.JsonMappingException;
import io.a2a.jsonrpc.common.json.JsonProcessingException;
import io.a2a.jsonrpc.common.json.MethodNotFoundJsonMappingException;
import io.a2a.jsonrpc.common.wrappers.A2AErrorResponse;
import io.a2a.jsonrpc.common.wrappers.A2AResponse;
import io.a2a.jsonrpc.common.wrappers.CancelTaskRequest;
import io.a2a.jsonrpc.common.wrappers.CancelTaskResponse;
import io.a2a.jsonrpc.common.wrappers.DeleteTaskPushNotificationConfigRequest;
import io.a2a.jsonrpc.common.wrappers.DeleteTaskPushNotificationConfigResponse;
import io.a2a.jsonrpc.common.wrappers.GetAuthenticatedExtendedCardRequest;
import io.a2a.jsonrpc.common.wrappers.GetAuthenticatedExtendedCardResponse;
import io.a2a.jsonrpc.common.wrappers.GetTaskPushNotificationConfigRequest;
import io.a2a.jsonrpc.common.wrappers.GetTaskPushNotificationConfigResponse;
import io.a2a.jsonrpc.common.wrappers.GetTaskRequest;
import io.a2a.jsonrpc.common.wrappers.GetTaskResponse;
import io.a2a.jsonrpc.common.wrappers.ListTaskPushNotificationConfigRequest;
import io.a2a.jsonrpc.common.wrappers.ListTaskPushNotificationConfigResponse;
import io.a2a.jsonrpc.common.wrappers.ListTasksRequest;
import io.a2a.jsonrpc.common.wrappers.ListTasksResponse;
import io.a2a.jsonrpc.common.wrappers.NonStreamingJSONRPCRequest;
import io.a2a.jsonrpc.common.wrappers.SendMessageRequest;
import io.a2a.jsonrpc.common.wrappers.SendMessageResponse;
import io.a2a.jsonrpc.common.wrappers.SendStreamingMessageRequest;
import io.a2a.jsonrpc.common.wrappers.SendStreamingMessageResponse;
import io.a2a.jsonrpc.common.wrappers.SetTaskPushNotificationConfigRequest;
import io.a2a.jsonrpc.common.wrappers.SetTaskPushNotificationConfigResponse;
import io.a2a.jsonrpc.common.wrappers.StreamingJSONRPCRequest;
import io.a2a.jsonrpc.common.wrappers.SubscribeToTaskRequest;
import io.a2a.server.ServerCallContext;
import io.a2a.server.util.async.AsyncUtils;
import io.a2a.spec.A2AError;
import io.a2a.spec.InternalError;
import io.a2a.spec.InvalidParamsError;
import io.a2a.spec.InvalidRequestError;
import io.a2a.spec.JSONParseError;
import io.a2a.spec.MethodNotFoundError;
import io.a2a.transport.jsonrpc.context.JSONRPCContextKeys;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import mutiny.zero.ZeroPublisher;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.adapter.JdkFlowAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;

@RestController
public class MessageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    private final JSONRPCHandler jsonRpcHandler;
    private final CallContextFactory callContextFactory;
    private final Executor executor;

    public MessageController(JSONRPCHandler jsonRpcHandler, CallContextFactory callContextFactory, Executor executor) {
        this.jsonRpcHandler = jsonRpcHandler;
        this.callContextFactory = callContextFactory;
        this.executor = executor;
    }

    /**
     * Handles incoming POST requests to the main A2A endpoint. Dispatches the
     * request to the appropriate JSON-RPC handler method and returns the response.
     *
     * @param jsonRpcMessage the JSON-RPC request string
     * @return the JSON-RPC response which may be an error response
     */
    @PostMapping(
            path = {"", "/"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> sendMessage(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @RequestBody String jsonRpcMessage
    ) {
        LOGGER.debug("Handling non-streaming request");

        try {
            var request = JSONRPCUtils.parseRequestBody(jsonRpcMessage);
            var context = callContextFactory.build(exchange, authentication, request.getMethod());
            context.getState().put(JSONRPCContextKeys.METHOD_NAME_KEY, request.getMethod());

            if (request instanceof NonStreamingJSONRPCRequest<?> jsonRpcRequest) {
                var response = processNonStreamingRequest(jsonRpcRequest, context);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(serializeResponse(response));
            }

            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Unsupported streaming request type");
        } catch (Throwable ex) {
            var error = processErrorResponse(ex);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(serializeResponse(error));
        } finally {
            LOGGER.debug("Completed non-streaming request");
        }
    }

    /**
     * Handles incoming POST requests to the main A2A endpoint that involve Server-Sent Events (SSE).
     * Uses custom SSE response handling to avoid JAX-RS SSE compatibility issues with async publishers.
     */
    @PostMapping(
            path = {"", "/"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public ResponseEntity<?> sendMessageStreaming(
            ServerWebExchange exchange,
            @Nullable Authentication authentication,
            @RequestBody String jsonRpcMessage
    ) {
        try {
            var request = JSONRPCUtils.parseRequestBody(jsonRpcMessage);
            var context = callContextFactory.build(exchange, authentication, request.getMethod());
            context.getState().put(JSONRPCContextKeys.METHOD_NAME_KEY, request.getMethod());

            if (request instanceof StreamingJSONRPCRequest<?> streamingRequest) {
                var publisher = createStreamingPublisher(streamingRequest, context);
                LOGGER.debug("Created streaming publisher: {}", publisher);

                if (publisher != null) {
                    // Handle the streaming response with custom SSE formatting
                    LOGGER.debug("Handling custom SSE response for publisher: {}", publisher);

                    var textPublisher = convertToSendStreamingMessageResponse(publisher);
                    return ResponseEntity.ok()
                            .contentType(MediaType.TEXT_EVENT_STREAM)
                            .body(JdkFlowAdapter.flowPublisherToFlux(textPublisher));
                }
            }

            // Handle unsupported request types
            LOGGER.debug("Unsupported streaming request type: {}", request.getClass().getSimpleName());

            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body("Unsupported streaming request type");
        } catch (Throwable ex) {
            var error = processErrorResponse(ex);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(serializeResponse(error));
        } finally {
            LOGGER.debug("Completed streaming request");
        }
    }

    private A2AErrorResponse processErrorResponse(Throwable ex) {
        switch (ex) {
            case InvalidParamsJsonMappingException error -> {
                LOGGER.warn("Invalid params in request: {}", error.getMessage());
                return new A2AErrorResponse(
                        error.getId(),
                        new InvalidParamsError(error.getMessage())
                );
            }
            case MethodNotFoundJsonMappingException error -> {
                LOGGER.warn("Method not found in request: {}", error.getMessage());
                return new A2AErrorResponse(
                        error.getId(),
                        new MethodNotFoundError(null, error.getMessage(), null)
                );
            }
            case IdJsonMappingException error -> {
                LOGGER.warn("Invalid request ID: {}", error.getMessage());
                return new A2AErrorResponse(
                        error.getId(),
                        new InvalidRequestError(error.getMessage())
                );
            }
            case JsonMappingException error -> {
                LOGGER.warn("JSON mapping error: {}", error.getMessage(), error);
                // Check if this is a parse error wrapped in a mapping exception
                if (error.getCause() instanceof JsonProcessingException) {
                    return new A2AErrorResponse(
                            new JSONParseError(error.getMessage())
                    );
                } else {
                    // Otherwise it's an invalid request (valid JSON but doesn't match schema)
                    return new A2AErrorResponse(
                            new InvalidRequestError(error.getMessage())
                    );
                }
            }
            case JsonSyntaxException error -> {
                LOGGER.warn("JSON syntax error: {}", error.getMessage());
                return new A2AErrorResponse(
                        new JSONParseError(error.getMessage())
                );
            }
            case JsonProcessingException error -> {
                LOGGER.warn("JSON processing error: {}", error.getMessage());
                return new A2AErrorResponse(
                        new JSONParseError(error.getMessage())
                );
            }
            case A2AError error -> {
                LOGGER.warn("Error processing request: {}", error.getMessage());
                return new A2AErrorResponse(error);
            }
            default -> {
                LOGGER.error("Unexpected error processing request: {}", ex.getMessage(), ex);
                return new A2AErrorResponse(
                        new InternalError(ex.getMessage())
                );
            }
        }
    }

    private A2AResponse<?> processNonStreamingRequest(
            NonStreamingJSONRPCRequest<?> request,
            ServerCallContext context
    ) {
        return switch (request) {
            case GetTaskRequest taskRequest -> jsonRpcHandler.onGetTask(taskRequest, context);
            case CancelTaskRequest taskRequest -> jsonRpcHandler.onCancelTask(taskRequest, context);
            case ListTasksRequest tasksRequest -> jsonRpcHandler.onListTasks(tasksRequest, context);
            case SetTaskPushNotificationConfigRequest taskRequest ->
                    jsonRpcHandler.setPushNotificationConfig(taskRequest, context);
            case GetTaskPushNotificationConfigRequest taskRequest ->
                    jsonRpcHandler.getPushNotificationConfig(taskRequest, context);
            case SendMessageRequest sendMessageRequest -> jsonRpcHandler.onMessageSend(sendMessageRequest, context);
            case ListTaskPushNotificationConfigRequest taskRequest ->
                    jsonRpcHandler.listPushNotificationConfig(taskRequest, context);
            case DeleteTaskPushNotificationConfigRequest taskRequest ->
                    jsonRpcHandler.deletePushNotificationConfig(taskRequest, context);
            case GetAuthenticatedExtendedCardRequest cardRequest ->
                    jsonRpcHandler.onGetAuthenticatedExtendedCardRequest(
                            cardRequest,
                            context
                    );
        };
    }

    /**
     * Creates a streaming publisher for the given request.
     * This method runs synchronously to avoid connection closure issues.
     */
    private Flow.Publisher<SendStreamingMessageResponse> createStreamingPublisher(
            StreamingJSONRPCRequest<?> request,
            ServerCallContext context
    ) {
        return switch (request) {
            case SendStreamingMessageRequest req -> jsonRpcHandler.onMessageSendStream(req, context);
            case SubscribeToTaskRequest req -> jsonRpcHandler.onSubscribeToTask(req, context);
            default -> null;
        };
    }

    /**
     * Handles the streaming response using custom SSE formatting.
     * This approach avoids JAX-RS SSE compatibility issues with async publishers.
     */
    private Flow.Publisher<String> convertToSendStreamingMessageResponse(
            Flow.Publisher<SendStreamingMessageResponse> publisher
    ) {
        // We can't use the normal convertingProcessor since that propagates any errors as an error handled
        // via Subscriber.onError() rather than as part of the SendStreamingResponse payload
        return ZeroPublisher.create(AsyncUtils.createTubeConfig(), tube -> {
            CompletableFuture.runAsync(() -> {
                publisher.subscribe(new Flow.Subscriber<>() {
                    private Flow.Subscription subscription;

                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        LOGGER.debug("Custom SSE subscriber onSubscribe called");

                        this.subscription = subscription;
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(SendStreamingMessageResponse item) {
                        LOGGER.debug("Custom SSE subscriber onNext called with item: {}", item);

                        try {
                            var jsonData = serializeResponse(item);
                            tube.send(jsonData);

                            if (subscription != null) {
                                subscription.request(1);
                            }
                            LOGGER.debug("Custom SSE event sent successfully");
                        } catch (Exception ex) {
                            onError(ex);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        var error = processErrorResponse(throwable);
                        var payload = serializeResponse(error);
                        tube.send(payload);

                        onComplete();
                    }

                    @Override
                    public void onComplete() {
                        tube.complete();
                    }
                });
            }, executor);
        });
    }

    /**
     * Serializes A2A responses to JSON using protobuf conversion.
     * This ensures enum values are serialized correctly using protobuf JSON format.
     */
    private String serializeResponse(A2AResponse<?> response) {
        // For error responses, use Jackson serialization (errors are standardized)
        if (response instanceof A2AErrorResponse || response.getError() != null) {
            return JSONRPCUtils.toJsonRPCErrorResponse(response.getId(), response.getError());
        }

        // Convert domain response to protobuf message and serialize
        var protoMessage = convertToProto(response);
        return JSONRPCUtils.toJsonRPCResultResponse(response.getId(), protoMessage);
    }

    /**
     * Converts A2A response objects to their protobuf equivalents.
     */
    private com.google.protobuf.MessageOrBuilder convertToProto(A2AResponse<?> response) {
        return switch (response) {
            case GetTaskResponse r -> ProtoUtils.ToProto.task(r.getResult());
            case CancelTaskResponse r -> ProtoUtils.ToProto.task(r.getResult());
            case ListTasksResponse r -> ProtoUtils.ToProto.listTasksResult(r.getResult());
            case SetTaskPushNotificationConfigResponse r ->
                    ProtoUtils.ToProto.setTaskPushNotificationConfigResponse(r.getResult());
            case GetTaskPushNotificationConfigResponse r ->
                    ProtoUtils.ToProto.getTaskPushNotificationConfigResponse(r.getResult());
            case ListTaskPushNotificationConfigResponse r ->
                    ProtoUtils.ToProto.listTaskPushNotificationConfigResponse(r.getResult());
            case DeleteTaskPushNotificationConfigResponse deleteTaskPushNotificationConfigResponse ->
                // DeleteTaskPushNotificationConfig has no result body, just return empty message
                    com.google.protobuf.Empty.getDefaultInstance();
            case SendMessageResponse r -> ProtoUtils.ToProto.taskOrMessage(r.getResult());
            case SendStreamingMessageResponse r -> ProtoUtils.ToProto.taskOrMessageStream(r.getResult());
            case GetAuthenticatedExtendedCardResponse r ->
                    ProtoUtils.ToProto.getAuthenticatedExtendedCardResponse(r.getResult());
            default -> throw new IllegalArgumentException("Unknown response type: " + response.getClass().getName());
        };
    }
}
