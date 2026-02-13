package io.github.timo.a2a.spring.ai.agent.example.tools

import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service
import kotlin.text.equals

@Service
class OrderAgentTools {
    data class Order(
        val id: String,
        val type: String,
        val sellerId: String,
        val amount: Double,
        val customerName: String
    )

    @Suppress("MagicNumber")
    private val orders = listOf(
        Order("1", "B2B", "1", 100.50, "James"),
        Order("2", "B2B", "2", 50.75, "William"),
        Order("3", "B2C", "3", 130.00, "Oliver"),
        Order("4", "B2C", "4", 330.00, "Ethan"),
        Order("5", "B2B", "4", 12.34, "John"),
    )

    @Tool(name = "getB2BOrders", description = "Gets and returns all B2B orders")
    fun getB2BOrders(): List<Order> {
        return orders.filter { it.type.equals("B2B", true) }
    }

    @Tool(name = "getB2COrders", description = "Gets and returns all B2C orders")
    fun getB2COrders(): List<Order> {
        return orders.filter { it.type.equals("B2C", true) }
    }
}
