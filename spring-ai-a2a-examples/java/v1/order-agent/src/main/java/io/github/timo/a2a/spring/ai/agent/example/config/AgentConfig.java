package io.github.timo.a2a.spring.ai.agent.example.config;

import io.github.timo.a2a.spring.ai.agent.example.tools.OrderAgentTools;
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

    private static final String ORDER_AGENT_SYSTEM_INSTRUCTION = """
        You are a specialized order management assistant.
        Your primary function is to utilize the provided tools to retrieve and relay B2B and B2C orders information in response to user queries.
        You must rely exclusively on these tools for data and refrain from inventing information.
        Ensure that all responses include the detailed output from the tools used and are formatted in text or json
        """;

    @Bean
    public AgentCard agentCard(SpringA2AServerProperties webServerProperties) {
        AgentProvider provider = new AgentProvider(
            "Timo",
            "https://github.com/goodfriend2ks/a2a-java-sdk-spring"
        );

        AgentCapabilities capabilities = AgentCapabilities.builder()
            .streaming(true)
            .pushNotifications(true)
            .stateTransitionHistory(true)
            .build();

        var supportedInterfaces = webServerProperties.buildAgentInterfaces();

        return AgentCard.builder()
            .name("Order Agent")
            .description("Provides B2B and B2C order information")
            .version("1.0.0")
            .provider(provider)
            .capabilities(capabilities)
            .defaultInputModes(Arrays.asList("text"))
            .defaultOutputModes(Arrays.asList("json", "text"))
            .supportedInterfaces(supportedInterfaces)
            .skills(
                Arrays.asList(
                    AgentSkill.builder()
                        .id("b2b-orders")
                        .name("B2B Orders")
                        .description("fetches all B2B orders")
                        .tags(Arrays.asList("b2b", "orders", "list"))
                        .examples(Arrays.asList(
                            "Show me all B2B orders",
                            "List B2B orders",
                            "Get B2B orders",
                            "Fetch B2B orders",
                            "Display B2B orders"
                        ))
                        .inputModes(Arrays.asList("text"))
                        .outputModes(Arrays.asList("json", "text"))
                        .build(),

                    AgentSkill.builder()
                        .id("b2c-orders")
                        .name("B2C Orders")
                        .description("fetches all B2C orders")
                        .tags(Arrays.asList("b2c", "orders", "list"))
                        .examples(Arrays.asList(
                            "Show me all B2Corders",
                            "List B2C orders",
                            "Get B2C orders",
                            "Fetch B2C orders",
                            "Display B2C orders"
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
        OrderAgentTools orderAgentTools
    ) {
        ChatClient chatClient = chatClientBuilder.clone()
            .defaultSystem(ORDER_AGENT_SYSTEM_INSTRUCTION)
            .defaultTools(orderAgentTools)
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
