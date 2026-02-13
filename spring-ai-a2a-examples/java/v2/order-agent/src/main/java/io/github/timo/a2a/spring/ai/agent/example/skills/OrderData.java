package io.github.timo.a2a.spring.ai.agent.example.skills;

import java.util.Arrays;
import java.util.List;

public class OrderData {
    public static final List<Order> ORDERS = Arrays.asList(
        new Order("1", "B2B", "1", 100.50, "James"),
        new Order("2", "B2B", "2", 50.75, "William"),
        new Order("3", "B2C", "3", 130.00, "Oliver"),
        new Order("4", "B2C", "4", 330.00, "Ethan"),
        new Order("5", "B2B", "4", 12.34, "John")
    );
}
