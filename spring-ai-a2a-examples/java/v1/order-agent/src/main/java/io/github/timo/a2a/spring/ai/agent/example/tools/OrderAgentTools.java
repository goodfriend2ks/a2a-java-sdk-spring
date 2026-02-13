package io.github.timo.a2a.spring.ai.agent.example.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderAgentTools {

    public static class Order {
        private String id;
        private String type;
        private String sellerId;
        private Double amount;
        private String customerName;

        public Order(String id, String type, String sellerId, Double amount, String customerName) {
            this.id = id;
            this.type = type;
            this.sellerId = sellerId;
            this.amount = amount;
            this.customerName = customerName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSellerId() {
            return sellerId;
        }

        public void setSellerId(String sellerId) {
            this.sellerId = sellerId;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }
    }

    private final List<Order> orders = Arrays.asList(
        new Order("1", "B2B", "1", 100.50, "James"),
        new Order("2", "B2B", "2", 50.75, "William"),
        new Order("3", "B2C", "3", 130.00, "Oliver"),
        new Order("4", "B2C", "4", 330.00, "Ethan"),
        new Order("5", "B2B", "4", 12.34, "John")
    );

    @Tool(name = "getB2BOrders", description = "Gets and returns all B2B orders")
    public List<Order> getB2BOrders() {
        return orders.stream()
            .filter(order -> order.getType().equalsIgnoreCase("B2B"))
            .collect(Collectors.toList());
    }

    @Tool(name = "getB2COrders", description = "Gets and returns all B2C orders")
    public List<Order> getB2COrders() {
        return orders.stream()
            .filter(order -> order.getType().equalsIgnoreCase("B2C"))
            .collect(Collectors.toList());
    }
}
