package io.github.timo.a2a.spring.ai.agent.example.skills;

import io.github.timo.a2a.spring.ai.agent.annotations.AgentSkill;
import io.github.timo.a2a.spring.ai.agent.annotations.SkillAction;

import java.util.List;
import java.util.stream.Collectors;

@AgentSkill(
    id = "b2b-order",
    name = "B2B Orders",
    description = "fetches all B2B orders",
    tags = {"b2b", "orders", "list"},
    examples = {
        "Show me all B2B orders",
        "List B2B orders",
        "Get B2B orders",
        "Fetch B2B orders",
        "Display B2B orders"
    },
    inputModes = {"text"},
    outputModes = {"text", "json"}
)
public class B2BOrderAgentSkill {

    @SkillAction(
        name = "getB2BOrders",
        description = "Gets and returns all B2B orders"
    )
    public List<Order> getB2BOrders() {
        return OrderData.ORDERS.stream()
            .filter(order -> order.getType().equalsIgnoreCase("B2B"))
            .collect(Collectors.toList());
    }

    @SkillAction(
        name = "getB2BOrderById",
        description = "Gets and returns B2B order by id"
    )
    public Order getB2BOrder(String id) {
        return OrderData.ORDERS.stream()
            .filter(order -> order.getId().equals(id) && order.getType().equalsIgnoreCase("B2B"))
            .findFirst()
            .orElse(null);
    }
}
