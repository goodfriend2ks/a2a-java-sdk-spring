package com.timo.ai.a2a.server.rest.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import com.timo.ai.a2a.server.rest.RestErrorResponse;
import io.a2a.grpc.Message;
import io.a2a.grpc.Part;
import io.a2a.grpc.Role;
import io.a2a.grpc.SendMessageConfiguration;
import io.a2a.grpc.SendMessageRequest;
import io.a2a.grpc.SendMessageResponse;
import io.a2a.grpc.utils.ProtoUtils;
import io.a2a.server.ServerCallContext;
import io.a2a.server.extensions.A2AExtensions;
import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.server.util.async.AsyncUtils;
import io.a2a.server.version.A2AVersionValidator;
import io.a2a.spec.A2AError;
import io.a2a.spec.AgentCard;
import io.a2a.spec.InternalError;
import io.a2a.spec.InvalidRequestError;
import io.a2a.spec.StreamingEventKind;
import io.a2a.transport.rest.handler.RestHandler;
import jakarta.enterprise.inject.Instance;
import mutiny.zero.ZeroPublisher;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;

public class AgentRestHandler extends RestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentRestHandler.class);

    private final AgentCard agentCard;
    private final RequestHandler requestHandler;
    private final Executor executor;

    public AgentRestHandler(AgentCard agentCard, Instance<AgentCard> extendedAgentCard,
                            RequestHandler requestHandler, Executor executor) {
        super(agentCard, extendedAgentCard, requestHandler, executor);

        this.agentCard = agentCard;
        this.requestHandler = requestHandler;
        this.executor = executor;
    }

    public AgentRestHandler(AgentCard agentCard, RequestHandler requestHandler, Executor executor) {
        super(agentCard, requestHandler, executor);

        this.agentCard = agentCard;
        this.requestHandler = requestHandler;
        this.executor = executor;
    }

    public HTTPRestResponse sendUserMessage(
            String message,
            String tenant,
            ServerCallContext context,
            @Nullable
            String contextId,
            @Nullable
            String taskId
    ) {
        try {
            var request = buildSendUserMessageRequestBuilder(message, tenant, context, contextId, taskId);
            var result = requestHandler.onMessageSend(ProtoUtils.FromProto.messageSendParams(request), context);

            return createSuccessResponse(
                    HttpStatus.OK.value(),
                    SendMessageResponse.newBuilder(ProtoUtils.ToProto.taskOrMessage(result))
            );
        } catch (A2AError ex) {
            LOGGER.error("Error sending user message", ex);
            return createErrorResponse(ex);
        } catch (Throwable throwable) {
            LOGGER.error("Error sending user message", throwable);
            return createErrorResponse(new InternalError(throwable.getMessage()));
        }
    }

    public HTTPRestResponse sendStreamingUserMessage(
            String message,
            String tenant,
            ServerCallContext context,
            @Nullable
            String contextId,
            @Nullable
            String taskId
    ) {
        try {
            if (!agentCard.capabilities().streaming()) {
                return createErrorResponse(new InvalidRequestError("Streaming is not supported by the agent"));
            }

            var request = buildSendUserMessageRequestBuilder(message, tenant, context, contextId, taskId);
            var publisher = requestHandler.onMessageSendStream(
                    ProtoUtils.FromProto.messageSendParams(request),
                    context
            );

            return new HTTPRestStreamingResponse(
                    convertToSendStreamingMessageResponse(publisher)
            );
        } catch (A2AError ex) {
            return new HTTPRestStreamingResponse(
                    ZeroPublisher.fromItems(new RestErrorResponse(ex).toJson())
            );
        } catch (Throwable throwable) {
            return new HTTPRestStreamingResponse(
                    ZeroPublisher.fromItems(
                            new RestErrorResponse(new InternalError(throwable.getMessage())).toJson()
                    )
            );
        }
    }

    private SendMessageRequest.Builder buildSendUserMessageRequestBuilder(
            String message,
            String tenant,
            ServerCallContext context,
            @Nullable
            String contextId,
            @Nullable
            String taskId
    ) {
        A2AVersionValidator.validateProtocolVersion(agentCard, context);
        A2AExtensions.validateRequiredExtensions(agentCard, context);

        var userMessageBuilder = Message.newBuilder()
                .addParts(Part.newBuilder().setText(message))
                .setRole(Role.ROLE_USER)
                .setMessageId(UUID.randomUUID().toString());

        if (contextId != null) {
            userMessageBuilder.setContextId(contextId);
        }

        if (taskId != null) {
            userMessageBuilder.setTaskId(taskId);
        }

        return SendMessageRequest.newBuilder()
                .setRequest(userMessageBuilder.build())
                .setConfiguration(SendMessageConfiguration.newBuilder())
                .setMetadata(Struct.newBuilder())
                .setTenant(tenant);
    }

    private HTTPRestResponse createSuccessResponse(int statusCode, com.google.protobuf.Message.Builder builder) {
        try {
            // Include default value fields to ensure empty arrays, zeros, etc. are present in JSON
            String jsonBody = JsonFormat.printer().alwaysPrintFieldsWithNoPresence().print(builder);
            return new HTTPRestResponse(statusCode, "application/json", jsonBody);
        } catch (InvalidProtocolBufferException ex) {
            return createErrorResponse(new InternalError("Failed to serialize response: " + ex.getMessage()));
        }
    }

    private Flow.Publisher<String> convertToSendStreamingMessageResponse(
            Flow.Publisher<StreamingEventKind> publisher
    ) {
        // We can't use the normal convertingProcessor since that propagates any errors as an error handled
        // via Subscriber.onError() rather than as part of the SendStreamingResponse payload
        return ZeroPublisher.create(AsyncUtils.createTubeConfig(), tube -> {
            CompletableFuture.runAsync(() -> {
                publisher.subscribe(new Flow.Subscriber<>() {
                    JsonFormat.Printer jsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace();
                    Flow.Subscription subscription = null;

                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(StreamingEventKind item) {
                        try {
                            String payload = jsonPrinter.print(ProtoUtils.ToProto.taskOrMessageStream(item));
                            tube.send(payload);
                            if (subscription != null) {
                                subscription.request(1);
                            }
                        } catch (InvalidProtocolBufferException ex) {
                            onError(ex);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (throwable instanceof A2AError jsonrpcError) {
                            tube.send(new RestErrorResponse(jsonrpcError).toJson());
                        } else {
                            tube.send(new RestErrorResponse(new InternalError(throwable.getMessage())).toJson());
                        }
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
}
