package com.stocktrading.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;
    private String email;
    private double balance;  // cash balance in USD

    // symbol -> quantity owned
    private Map<String, Integer> portfolio = new HashMap<>();

    // symbol -> average buy price
    private Map<String, Double> avgBuyPrice = new HashMap<>();

    public double getTotalInvested(Map<String, Stock> stockMap) {
        double total = 0;
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            Stock stock = stockMap.get(entry.getKey());
            if (stock != null) {
                double avg = avgBuyPrice.getOrDefault(entry.getKey(), 0.0);
                total += avg * entry.getValue();
            }
        }
        return total;
    }

    public double getCurrentPortfolioValue(Map<String, Stock> stockMap) {
        double total = 0;
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            Stock stock = stockMap.get(entry.getKey());
            if (stock != null) {
                total += stock.getCurrentPrice() * entry.getValue();
            }
        }
        return total;
    }
}
