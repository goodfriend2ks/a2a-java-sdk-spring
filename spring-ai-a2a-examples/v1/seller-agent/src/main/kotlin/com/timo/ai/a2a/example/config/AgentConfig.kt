package com.timo.ai.a2a.example.config

import com.timo.ai.a2a.example.tools.SellerAgentTools
import com.timo.ai.a2a.server.SpringA2AServerProperties
import com.timo.ai.a2a.server.executor.AgentExecutorHandler
import io.a2a.spec.AgentCapabilities
import io.a2a.spec.AgentCard
import io.a2a.spec.AgentInterface
import io.a2a.spec.AgentProvider
import io.a2a.spec.AgentSkill
import io.a2a.spec.TextPart
import io.a2a.spec.TransportProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AgentConfig {
    companion object {
        private val SELLER_AGENT_SYSTEM_INSTRUCTION = """
            You are a specialized seller management assistant.
            Your primary function is to utilize the provided tools to retrieve and relay seller information in response to user queries.
            You must rely exclusively on these tools for data and refrain from inventing information.
            Ensure that all responses include the detailed output from the tools used and are formatted in text or json
        """.trimIndent()
    }

    @Bean
    @Suppress("LongMethod")
    fun agentCard(
        webServerProperties: SpringA2AServerProperties,
    ): AgentCard {
        val provider = AgentProvider(
            "MFV",
            "https://careers.moneyforward.vn/"
        )

        val capabilities = AgentCapabilities.builder()
            .streaming(true)
            .pushNotifications(true)
            .stateTransitionHistory(true)
            .build()

        val supportedInterfaces = webServerProperties.buildAgentInterfaces()

        return AgentCard.builder()
            .name("Seller Agent")
            .description("Provides seller information")
            .version("1.0.0")
            .provider(provider)
            .capabilities(capabilities)
            .defaultInputModes(
                listOf("text")
            )
            .defaultOutputModes(
                listOf("json", "text")
            )
            .supportedInterfaces(supportedInterfaces)
            .skills(
                listOf(
                    AgentSkill.builder()
                        .id("seller-by-id")
                        .name("Seller By Id")
                        .description("Returns Seller By Id")
                        .tags(listOf("seller", "seller id", "seller info", "seller information"))
                        .examples(
                            listOf(
                                "Give me seller by id", "Give me seller information by id",
                                "Give me seller info by id", "Get Seller Info By Id",
                                "Get Seller Id", "Get Seller Information by Id"
                            )
                        )
                        .inputModes(listOf("text"))
                        .outputModes(listOf("json", "text"))
                        .build(),

                    AgentSkill.builder()
                        .id("seller-by-name")
                        .name("Seller By name")
                        .description("Returns Seller By name")
                        .tags(listOf("seller", "seller name", "seller info", "seller information"))
                        .examples(
                            listOf(
                                "Give me seller by name", "Give me seller information by name",
                                "Give me seller info by name", "Get Seller Info By name",
                                "Get Seller name", "Get Seller Information by name"
                            )
                        )
                        .inputModes(listOf("text"))
                        .outputModes(listOf("json", "text"))
                        .build()
                )
            )
            .protocolVersion("1.0")
            .build()
    }

    @Bean
    fun agentExecutorHandler(
        chatClientBuilder: ChatClient.Builder,
        sellerAgentTools: SellerAgentTools
    ): AgentExecutorHandler {
        val chatClient = chatClientBuilder.clone()
            .defaultSystem(SELLER_AGENT_SYSTEM_INSTRUCTION)
            .defaultTools(sellerAgentTools)
            .build()

        return AgentExecutorHandler { requestContext ->
            val userMessage = AgentExecutorHandler.extractTextFromMessage(requestContext.message)
            val result = runBlocking {
                CoroutineScope(Dispatchers.IO).async {
                    chatClient.prompt()
                        .user(userMessage)
                        .call()
                        .content()
                }.await()
            }

            TextPart(result)
        }
    }
}
