package io.github.timo.a2a.spring.ai.agent.example.config;

import io.github.timo.a2a.spring.ai.agent.example.tools.SellerAgentTools;
import io.github.timo.a2a.server.SpringA2AServerProperties;
import io.github.timo.a2a.server.executor.AgentExecutorHandler;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentProvider;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.TextPart;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Configuration
public class AgentConfig {

    private static final String SELLER_AGENT_SYSTEM_INSTRUCTION = """
        You are a specialized seller management assistant.
        Your primary function is to utilize the provided tools to retrieve and relay seller information in response to user queries.
        You must rely exclusively on these tools for data and refrain from inventing information.
        Ensure that all responses include the detailed output from the tools used and are formatted in text or json
        """;

    @Bean
    public AgentCard agentCard(SpringA2AServerProperties webServerProperties) {
        AgentProvider provider = new AgentProvider(
            "MFV",
            "https://careers.moneyforward.vn/"
        );

        AgentCapabilities capabilities = AgentCapabilities.builder()
            .streaming(true)
            .pushNotifications(true)
            .stateTransitionHistory(true)
            .build();

        var supportedInterfaces = webServerProperties.buildAgentInterfaces();

        return AgentCard.builder()
            .name("Seller Agent")
            .description("Provides seller information")
            .version("1.0.0")
            .provider(provider)
            .capabilities(capabilities)
            .defaultInputModes(Arrays.asList("text"))
            .defaultOutputModes(Arrays.asList("json", "text"))
            .supportedInterfaces(supportedInterfaces)
            .skills(
                Arrays.asList(
                    AgentSkill.builder()
                        .id("seller-by-id")
                        .name("Seller By Id")
                        .description("Returns Seller By Id")
                        .tags(Arrays.asList("seller", "seller id", "seller info", "seller information"))
                        .examples(Arrays.asList(
                            "Give me seller by id", "Give me seller information by id",
                            "Give me seller info by id", "Get Seller Info By Id",
                            "Get Seller Id", "Get Seller Information by Id"
                        ))
                        .inputModes(Arrays.asList("text"))
                        .outputModes(Arrays.asList("json", "text"))
                        .build(),

                    AgentSkill.builder()
                        .id("seller-by-name")
                        .name("Seller By name")
                        .description("Returns Seller By name")
                        .tags(Arrays.asList("seller", "seller name", "seller info", "seller information"))
                        .examples(Arrays.asList(
                            "Give me seller by name", "Give me seller information by name",
                            "Give me seller info by name", "Get Seller Info By name",
                            "Get Seller name", "Get Seller Information by name"
                        ))
                        .inputModes(Arrays.asList("text"))
                        .outputModes(Arrays.asList("json", "text"))
                        .build()
                )
            )
            .protocolVersion("1.0")
            .build();
    }

    @Bean
    public AgentExecutorHandler agentExecutorHandler(
        ChatClient.Builder chatClientBuilder,
        SellerAgentTools sellerAgentTools
    ) {
        ChatClient chatClient = chatClientBuilder.clone()
            .defaultSystem(SELLER_AGENT_SYSTEM_INSTRUCTION)
            .defaultTools(sellerAgentTools)
            .build();

        return requestContext -> {
            String userMessage = AgentExecutorHandler.extractTextFromMessage(requestContext.getMessage());

            String result = CompletableFuture.supplyAsync(() ->
                chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content()
            ).join();

            return new TextPart(result);
        };
    }
}
