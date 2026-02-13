package io.github.timo.a2a.spring.ai.agent.example.skills;

public class Order {
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
