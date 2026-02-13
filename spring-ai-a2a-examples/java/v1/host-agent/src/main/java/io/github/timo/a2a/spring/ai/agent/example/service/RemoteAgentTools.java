package io.github.timo.a2a.spring.ai.agent.example.service;

import io.a2a.client.Client;
import io.a2a.client.ClientEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.TaskUpdateEvent;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.transport.grpc.GrpcTransport;
import io.a2a.client.transport.grpc.GrpcTransportConfigBuilder;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.client.transport.rest.RestTransport;
import io.a2a.client.transport.rest.RestTransportConfig;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Message;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TextPart;
import io.a2a.spec.TransportProtocol;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class RemoteAgentTools {

    private final Logger logger = LoggerFactory.getLogger(RemoteAgentTools.class);
    private final RemoteAgentConnections remoteAgentConnections;

    public RemoteAgentTools(RemoteAgentConnections remoteAgentConnections) {
        this.remoteAgentConnections = remoteAgentConnections;
    }

    /**
     * Sends a task to a remote agent and returns the response.
     * @param agentName The name of the agent to send the task to
     * @param task The task description to send
     * @return The response from the remote agent
     */
    @Tool(description = "Sends a task to a remote agent. Use this to delegate work to specialized agents.")
    public String sendMessage(
        @ToolParam(description = "The name of the agent to send the task to") String agentName,
        @ToolParam(description = "The comprehensive task description and context to send to the agent") String task
    ) {
        logger.info("Sending message to agent '{}': {}", agentName, task);

        AgentCard agentCard = remoteAgentConnections.getRemoteAgentCards().get(agentName);
        if (agentCard == null) {
            String availableAgents = String.join(", ", remoteAgentConnections.getAgentNames());
            return "Agent '" + agentName + "' not found. Available agents: " + availableAgents;
        }

        try {
            boolean grpcSupport = agentCard.supportedInterfaces().stream()
                .anyMatch(iface -> iface.protocolBinding().equals(TransportProtocol.GRPC.asString()));
            boolean jsonRpcSupport = agentCard.supportedInterfaces().stream()
                .anyMatch(iface -> iface.protocolBinding().equals(TransportProtocol.JSONRPC.asString()));
            boolean restSupport = agentCard.supportedInterfaces().stream()
                .anyMatch(iface -> iface.protocolBinding().equals(TransportProtocol.HTTP_JSON.asString()));

            // Create the message
            Message message = Message.builder()
                .role(Message.Role.USER)
                .parts(Collections.singletonList(new TextPart(task)))
                .build();

            // Use CompletableFuture to wait for the response
            CompletableFuture<String> responseFuture = new CompletableFuture<>();
            AtomicReference<String> responseText = new AtomicReference<>("");

            // Create consumer
            var consumer = new java.util.function.BiConsumer<ClientEvent, AgentCard>() {
                @Override
                public void accept(ClientEvent event, AgentCard card) {
                    Task completedTask = null;
                    if (event instanceof TaskEvent taskEvent) {
                        completedTask = taskEvent.getTask();
                    } else if (event instanceof TaskUpdateEvent taskUpdateEvent) {
                        completedTask = taskUpdateEvent.getTask();
                    }

                    if (completedTask != null) {
                        // Extract text from artifacts
                        if (completedTask.artifacts() != null) {
                            String text = completedTask.artifacts().stream()
                                .flatMap(artifact -> artifact.parts().stream())
                                .filter(part -> part instanceof TextPart)
                                .map(part -> ((TextPart) part).text())
                                .collect(Collectors.joining(""));
                            responseText.set(text);
                        }

                        logger.info("Received task response: status={}", completedTask.status().state());
                        logger.info("Received task response: content={}", responseText.get());

                        if (completedTask.status().state() == TaskState.COMPLETED) {
                            responseFuture.complete(responseText.get());
                        }
                    } else {
                        logger.info("Received task event: {}", event);
                    }
                }
            };

            // Create client with consumer via builder
            ClientConfig clientConfig = new ClientConfig.Builder()
                .setAcceptedOutputModes(Collections.singletonList("text"))
                .build();

            var clientBuilder = Client.builder(agentCard)
                .clientConfig(clientConfig)
                .addConsumers(Collections.singletonList(consumer));

            if (grpcSupport) {
                clientBuilder.withTransport(
                    GrpcTransport.class,
                    new GrpcTransportConfigBuilder().channelFactory(target ->
                        ManagedChannelBuilder.forTarget(target).usePlaintext().build()
                    )
                );
            }

            if (jsonRpcSupport) {
                clientBuilder.withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig());
            }

            if (restSupport) {
                clientBuilder.withTransport(RestTransport.class, new RestTransportConfig());
            }

            Client client = clientBuilder.build();
            client.sendMessage(message);

            // Wait for response (with timeout)
            String result = responseFuture.get(60, TimeUnit.SECONDS);
            logger.info("Agent '{}' response: {}", agentName, result);
            return result;
        } catch (Exception ex) {
            logger.error("Error sending message to agent '{}': {}", agentName, ex.getMessage());
            return "Error communicating with agent '" + agentName + "': " + ex.getMessage();
        }
    }
}
