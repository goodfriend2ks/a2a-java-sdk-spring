package com.timo.ai.a2a.example.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RoutingController(
    private val chatClient: ChatClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/chat")
    suspend fun chat(@RequestBody userMessage: String): String? {
        logger.info("Received user message: {}", userMessage)

        val response = CoroutineScope(Dispatchers.IO).async {
            chatClient.prompt().user(userMessage).call().content()
        }.await()

        logger.info("Response: {}", response)
        return response
    }
}
