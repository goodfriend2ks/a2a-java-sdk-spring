package com.timo.ai.a2a.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SellerAgentApplication

fun main(args: Array<String>) {
    runApplication<SellerAgentApplication>(*args)
}
