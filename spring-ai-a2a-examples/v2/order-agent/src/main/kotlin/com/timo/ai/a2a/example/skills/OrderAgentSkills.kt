package com.timo.ai.a2a.example.skills

import com.timo.ai.a2a.annotations.AgentSkill
import com.timo.ai.a2a.annotations.SkillAction
import kotlin.text.equals

@AgentSkill(
    id = "b2b-order",
    name = "B2B Orders",
    description = "fetches all B2B orders",
    tags = ["b2b", "orders", "list"],
    examples = [
        "Show me all B2B orders",
        "List B2B orders",
        "Get B2B orders",
        "Fetch B2B orders",
        "Display B2B orders"
    ],
    inputModes = ["text"],
    outputModes = ["text", "json"],
)
class B2BOrderAgentSkill {
    @SkillAction(
        name = "getB2BOrders",
        description = "Gets and returns all B2B orders"
    )
    fun getB2BOrders(): List<Order> {
        return orders.filter { it.type.equals("B2B", true) }
    }

    @SkillAction(
        name = "getB2BOrderById",
        description = "Gets and returns B2B order by id"
    )
    fun getB2BOrder(id: String): Order? {
        return orders.firstOrNull { it.id == id && it.type.equals("B2B", true) }
    }
}

@AgentSkill(
    id = "b2c-order",
    name = "B2C Orders",
    description = "fetches all B2C orders",
    tags = ["b2c", "orders", "list"],
    examples = [
        "Show me all B2C orders",
        "List B2C orders",
        "Get B2C orders",
        "Fetch B2C orders",
        "Display B2C orders"
    ],
    inputModes = ["text"],
    outputModes = ["text", "json"],
)
class B2COrderAgentSkill {
    @SkillAction(
        name = "getB2COrders",
        description = "Gets and returns all B2C orders"
    )
    fun getB2COrders(): List<Order> {
        return orders.filter { it.type.equals("B2C", true) }
    }
}

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
