package io.github.timo.a2a.spring.ai.agent.example.tools

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service

@Service
class SellerAgentTools {
    data class Seller(
        val sellerId: String,
        val name: String,
        val address: String,
        val rating: Int
    )

    @Suppress("MagicNumber")
    private val sellerList = listOf(
        Seller("1", "Mike", "Paris", 7),
        Seller("2", "Harry", "Lyon", 9),
        Seller("3", "John", "Berlin", 5),
        Seller("4", "Daniel", "London", 4)
    )

    @Tool(name = "getSellerById", description = "Gets and returns seller by id")
    fun getSellerById(
        @ToolParam(description = "Seller Id") sellerId: String
    ): Seller? {
        return sellerList.firstOrNull { it.sellerId == sellerId }
    }

    @Tool(name = "getSellerByName", description = "Gets and returns seller by name")
    fun getSellerByName(
        @ToolParam(description = "Seller Name") sellerName: String?
    ): Seller? {
        return sellerList.firstOrNull { it.name == sellerName }
    }
}
