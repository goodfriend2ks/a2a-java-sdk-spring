package io.github.timo.a2a.spring.ai.agent.example.config;

import io.github.timo.a2a.spring.ai.agent.processor.AgentSkillsProvider;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Configuration
public class AgentConfig {

    private static final String ORDER_AGENT_SYSTEM_INSTRUCTION = """
        You are a specialized order management assistant.
        Your primary function is to utilize the provided tools to retrieve and relay B2B and B2C orders information in response to user queries.
        You must rely exclusively on these tools for data and refrain from inventing information.
        Ensure that all responses include the detailed output from the tools used and are formatted in text or json
        """;

    @Bean
    public AgentCard agentCard(
        SpringA2AServerProperties webServerProperties,
        AgentSkillsProvider agentSkillsProvider
    ) {
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
        List<AgentSkill> agentSkills = agentSkillsProvider.getAgentSkills();
        String agentDescription = agentSkillsProvider.buildAgentDescription();

        List<String> defaultInputModes = agentSkills.stream()
            .flatMap(skill -> skill.inputModes().stream())
            .map(mode -> mode.toLowerCase().trim())
            .distinct()
            .collect(Collectors.toList());
        if (defaultInputModes.isEmpty()) {
            defaultInputModes = List.of("text");
        }

        List<String> defaultOutputModes = agentSkills.stream()
            .flatMap(skill -> skill.outputModes().stream())
            .map(mode -> mode.toLowerCase().trim())
            .distinct()
            .collect(Collectors.toList());
        if (defaultOutputModes.isEmpty()) {
            defaultOutputModes = List.of("text");
        }

        return AgentCard.builder()
            .name("Order Agent")
            .description("Provides B2B and B2C order information. " + agentDescription)
            .version("1.0.0")
            .provider(provider)
            .capabilities(capabilities)
            .defaultInputModes(defaultInputModes)
            .defaultOutputModes(defaultOutputModes)
            .supportedInterfaces(supportedInterfaces)
            .skills(agentSkills)
            .protocolVersion("1.0")
            .build();
    }

    @Bean
    public AgentExecutorHandler agentExecutorHandler(
        ChatClient.Builder chatClientBuilder,
        AgentSkillsProvider agentSkillsProvider
    ) {
        ChatClient chatClient = chatClientBuilder.clone()
            .defaultSystem(ORDER_AGENT_SYSTEM_INSTRUCTION)
            .defaultToolCallbacks(agentSkillsProvider.getAgentActionCallbackProvider())
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
