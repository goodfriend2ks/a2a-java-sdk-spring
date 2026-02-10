package io.github.timo.a2a.spring.ai.agent.example.service

import io.a2a.client.Client
import io.a2a.client.ClientEvent
import io.a2a.client.TaskEvent
import io.a2a.client.TaskUpdateEvent
import io.a2a.client.config.ClientConfig
import io.a2a.client.transport.grpc.GrpcTransport
import io.a2a.client.transport.grpc.GrpcTransportConfigBuilder
import io.a2a.client.transport.jsonrpc.JSONRPCTransport
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig
import io.a2a.client.transport.rest.RestTransport
import io.a2a.client.transport.rest.RestTransportConfig
import io.a2a.spec.AgentCard
import io.a2a.spec.Message
import io.a2a.spec.TaskState
import io.a2a.spec.TextPart
import io.a2a.spec.TransportProtocol
import io.grpc.ManagedChannelBuilder
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.BiConsumer

@Service
class RemoteAgentTools(
    private val remoteAgentConnections: RemoteAgentConnections,
) {
    private val logger = LoggerFactory.getLogger(RemoteAgentTools::class.java)

    /**
     * Sends a task to a remote agent and returns the response.
     * @param agentName The name of the agent to send the task to
     * @param task The task description to send
     * @return The response from the remote agent
     */
    @Tool(description = "Sends a task to a remote agent. Use this to delegate work to specialized agents.")
    @Suppress("LongMethod", "ReturnCount", "MagicNumber")
    fun sendMessage(
        @ToolParam(description = "The name of the agent to send the task to") agentName: String,
        @ToolParam(description = "The comprehensive task description and context to send to the agent") task: String
    ): String {
        logger.info("Sending message to agent '{}': {}", agentName, task)

        val agentCard = remoteAgentConnections.remoteAgentCards[agentName]
        if (agentCard == null) {
            val availableAgents = remoteAgentConnections.getAgentNames().joinToString(", ")
            return "Agent '$agentName' not found. Available agents: $availableAgents"
        }

        try {
            val grpcSupport = agentCard.supportedInterfaces.any {
                it.protocolBinding == TransportProtocol.GRPC.asString()
            }
            val jsonRpcSupport = agentCard.supportedInterfaces.any {
                it.protocolBinding == TransportProtocol.JSONRPC.asString()
            }
            val restSupport = agentCard.supportedInterfaces.any {
                it.protocolBinding == TransportProtocol.HTTP_JSON.asString()
            }

            // Create the message
            val message = Message.builder()
                .role(Message.Role.USER)
                .parts(listOf(TextPart(task)))
                .build()

            // Use CompletableFuture to wait for the response
            val responseFuture = CompletableFuture<String>()
            val responseText = AtomicReference("")

            val consumer: BiConsumer<ClientEvent, AgentCard> = { event, _ ->
                val completedTask = when (event) {
                    is TaskEvent -> event.task
                    is TaskUpdateEvent -> event.task
                    else -> null
                }

                if (completedTask != null) {
                    // Extract text from artifacts
                    completedTask.artifacts?.joinToString("") { artifact ->
                        artifact.parts.filterIsInstance<TextPart>().joinToString("") { it.text }
                    }?.let {
                        responseText.set(it)
                    }

                    logger.info("Received task response: status={}", completedTask.status.state)
                    logger.info("Received task response: content={}", responseText.get())

                    if (completedTask.status.state == TaskState.COMPLETED) {
                        responseFuture.complete(responseText.get())
                    }
                } else {
                    logger.info("Received task event: {}", event)
                }
            }

            // Create client with consumer via builder
            val clientConfig = ClientConfig.Builder()
                .setAcceptedOutputModes(listOf("text"))
                .build()

            val clientBuilder = Client.builder(agentCard)
                .clientConfig(clientConfig)
                .addConsumers(listOf(consumer))

            if (grpcSupport) {
                clientBuilder.withTransport(
                    GrpcTransport::class.java,
                    GrpcTransportConfigBuilder().channelFactory {
                        ManagedChannelBuilder.forTarget(it).usePlaintext().build()
                    }
                )
            }

            if (jsonRpcSupport) {
                clientBuilder.withTransport(JSONRPCTransport::class.java, JSONRPCTransportConfig())
            }

            if (restSupport) {
                clientBuilder.withTransport(RestTransport::class.java, RestTransportConfig())
            }

            val client = clientBuilder.build()
            client.sendMessage(message)

            // Wait for response (with timeout)
            val result = responseFuture.get(60, TimeUnit.SECONDS)
            logger.info("Agent '{}' response: {}", agentName, result)
            return result
        } catch (@Suppress("TooGenericExceptionCaught") ex: Exception) {
            logger.error("Error sending message to agent '{}': {}", agentName, ex.message)
            return "Error communicating with agent '$agentName': ${ex.message}"
        }
    }
}
