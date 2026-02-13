package io.github.timo.a2a.spring.ai.agent.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class HostAgentApplication

fun main(args: Array<String>) {
    runApplication<HostAgentApplication>(*args)
}
