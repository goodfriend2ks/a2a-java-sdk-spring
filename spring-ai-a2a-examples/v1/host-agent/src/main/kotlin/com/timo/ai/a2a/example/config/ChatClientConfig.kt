package com.timo.ai.a2a.example.config

import com.timo.ai.a2a.example.service.RemoteAgentConnections
import com.timo.ai.a2a.example.service.RemoteAgentTools
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.text.trimIndent

@Configuration
class ChatClientConfig {
    companion object {
        @Suppress("MaxLineLength")
        private val ROUTING_SYSTEM_PROMPT = """
            **Role:** You are an expert Routing Delegator. Your primary function is to accurately delegate user inquiries regarding weather or accommodations to the appropriate specialized remote agents.
            
            **Core Directives:**
            
            * **Task Delegation:** Utilize the `sendMessage` function to assign actionable tasks to remote agents.
            * **Contextual Awareness for Remote Agents:** If a remote agent repeatedly requests user confirmation, assume it lacks access to the full conversation history. In such cases, enrich the task description with all necessary contextual information relevant to that specific agent.
            * **Autonomous Agent Engagement:** Never seek user permission before engaging with remote agents. If multiple agents are required to fulfill a request, connect with them directly without requesting user preference or confirmation.
            * **Transparent Communication:** Always present the complete and detailed response from the remote agent to the user.
            * **User Confirmation Relay:** If a remote agent asks for confirmation, and the user has not already provided it, relay this confirmation request to the user.
            * **Focused Information Sharing:** Provide remote agents with only relevant contextual information. Avoid extraneous details.
            * **No Redundant Confirmations:** Do not ask remote agents for confirmation of information or actions.
            * **Tool Reliance:** Strictly rely on available tools to address user requests. Do not generate responses based on assumptions. If information is insufficient, request clarification from the user.
            * **Prioritize Recent Interaction:** Focus primarily on the most recent parts of the conversation when processing requests.
            * **For seller information:** If don't have seller id or seller name, extract it from orders first.
            
            **Agent Router:**
            
            Available Agents:
            %s
        """.trimIndent()
    }

    private val logger = LoggerFactory.getLogger(ChatClientConfig::class.java)

    @Bean
    fun routingChatClient(
        chatClientBuilder: ChatClient.Builder,
        remoteAgentConnections: RemoteAgentConnections,
        remoteAgentTools: RemoteAgentTools
    ): ChatClient {
        val systemPrompt = String.format(ROUTING_SYSTEM_PROMPT, remoteAgentConnections.getAgentDescriptions())

        logger.info("Initializing routing ChatClient with agents: {}", remoteAgentConnections.getAgentNames())

        return chatClientBuilder
            .defaultSystem(systemPrompt)
            .defaultTools(remoteAgentTools)
            .build()
    }
}
