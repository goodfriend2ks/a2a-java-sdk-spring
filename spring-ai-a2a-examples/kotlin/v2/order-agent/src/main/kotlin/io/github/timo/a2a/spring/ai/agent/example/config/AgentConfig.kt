package io.github.timo.a2a.spring.ai.agent.example.config

import io.github.timo.a2a.spring.ai.agent.processor.AgentSkillsProvider
import io.github.timo.a2a.server.SpringA2AServerProperties
import io.github.timo.a2a.server.executor.AgentExecutorHandler
import io.a2a.spec.AgentCapabilities
import io.a2a.spec.AgentCard
import io.a2a.spec.AgentProvider
import io.a2a.spec.TextPart
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
        private val ORDER_AGENT_SYSTEM_INSTRUCTION = """
            You are a specialized order management assistant.
            Your primary function is to utilize the provided tools to retrieve and relay B2B and B2C orders information in response to user queries.
            You must rely exclusively on these tools for data and refrain from inventing information.
            Ensure that all responses include the detailed output from the tools used and are formatted in text or json
        """.trimIndent()
    }

    @Bean
    @Suppress("LongMethod")
    fun agentCard(
        webServerProperties: SpringA2AServerProperties,
        agentSkillsProvider: AgentSkillsProvider
    ): AgentCard {
        val provider = AgentProvider(
            "Timo",
            "https://github.com/goodfriend2ks/a2a-java-sdk-spring"
        )

        val capabilities = AgentCapabilities.builder()
            .streaming(true)
            .pushNotifications(true)
            .stateTransitionHistory(true)
            .build()

        val supportedInterfaces = webServerProperties.buildAgentInterfaces()
        val agentSkills = agentSkillsProvider.getAgentSkills()
        val agentDescription = agentSkillsProvider.buildAgentDescription()

        val defaultInputModes =
            agentSkills.flatMap { it.inputModes }.map { it.lowercase().trim() }.distinct().takeIf { it.isNotEmpty() }
                ?: listOf("text")
        val defaultOutputModes =
            agentSkills.flatMap { it.outputModes }.map { it.lowercase().trim() }.distinct().takeIf { it.isNotEmpty() }
                ?: listOf("text")

        return AgentCard.builder()
            .name("Order Agent")
            .description("Provides B2B and B2C order information. $agentDescription")
            .version("1.0.0")
            .provider(provider)
            .capabilities(capabilities)
            .defaultInputModes(defaultInputModes)
            .defaultOutputModes(defaultOutputModes)
            .supportedInterfaces(supportedInterfaces)
            .skills(agentSkills)
            .protocolVersion("1.0")
            .build()
    }

    @Bean
    fun agentExecutorHandler(
        chatClientBuilder: ChatClient.Builder,
        agentSkillsProvider: AgentSkillsProvider
    ): AgentExecutorHandler {
        val chatClient = chatClientBuilder.clone()
            .defaultSystem(ORDER_AGENT_SYSTEM_INSTRUCTION)
            .defaultToolCallbacks(agentSkillsProvider.agentActionCallbackProvider)
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
