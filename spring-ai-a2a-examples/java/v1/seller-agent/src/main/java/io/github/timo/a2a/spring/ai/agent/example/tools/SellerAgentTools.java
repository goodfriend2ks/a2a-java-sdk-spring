package io.github.timo.a2a.spring.ai.agent.example.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SellerAgentTools {

    public static class Seller {
        private String sellerId;
        private String name;
        private String address;
        private Integer rating;

        public Seller(String sellerId, String name, String address, Integer rating) {
            this.sellerId = sellerId;
            this.name = name;
            this.address = address;
            this.rating = rating;
        }

        public String getSellerId() {
            return sellerId;
        }

        public void setSellerId(String sellerId) {
            this.sellerId = sellerId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }
    }

    private final List<Seller> sellerList = Arrays.asList(
        new Seller("1", "Mike", "Paris", 7),
        new Seller("2", "Harry", "Lyon", 9),
        new Seller("3", "John", "Berlin", 5),
        new Seller("4", "Daniel", "London", 4)
    );

    @Tool(name = "getSellerById", description = "Gets and returns seller by id")
    public Seller getSellerById(@ToolParam(description = "Seller Id") String sellerId) {
        return sellerList.stream()
            .filter(seller -> seller.getSellerId().equals(sellerId))
            .findFirst()
            .orElse(null);
    }

    @Tool(name = "getSellerByName", description = "Gets and returns seller by name")
    public Seller getSellerByName(@ToolParam(description = "Seller Name") String sellerName) {
        return sellerList.stream()
            .filter(seller -> seller.getName().equals(sellerName))
            .findFirst()
            .orElse(null);
    }
}
