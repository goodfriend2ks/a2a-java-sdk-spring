package io.github.timo.a2a.spring.ai.agent.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class RoutingController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChatClient chatClient;

    public RoutingController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/chat")
    public Mono<String> chat(@RequestBody String userMessage) {
        logger.info("Received user message: {}", userMessage);

        return Mono.fromCallable(() -> {
                String response = chatClient.prompt().user(userMessage).call().content();
                logger.info("Response: {}", response);
                return response;
            })
            .subscribeOn(Schedulers.boundedElastic());
    }
}
