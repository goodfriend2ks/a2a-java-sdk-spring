package io.github.timo.a2a.spring.ai.agent.example.skills;

import io.github.timo.a2a.spring.ai.agent.annotations.AgentSkill;
import io.github.timo.a2a.spring.ai.agent.annotations.SkillAction;

import java.util.List;
import java.util.stream.Collectors;

@AgentSkill(
    id = "b2c-order",
    name = "B2C Orders",
    description = "fetches all B2C orders",
    tags = {"b2c", "orders", "list"},
    examples = {
        "Show me all B2C orders",
        "List B2C orders",
        "Get B2C orders",
        "Fetch B2C orders",
        "Display B2C orders"
    },
    inputModes = {"text"},
    outputModes = {"text", "json"}
)
public class B2COrderAgentSkill {

    @SkillAction(
        name = "getB2COrders",
        description = "Gets and returns all B2C orders"
    )
    public List<Order> getB2COrders() {
        return OrderData.ORDERS.stream()
            .filter(order -> order.getType().equalsIgnoreCase("B2C"))
            .collect(Collectors.toList());
    }
}
