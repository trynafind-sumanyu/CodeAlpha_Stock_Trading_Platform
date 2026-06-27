package com.stocktrading.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stocks")
public class Stock {

    @Id
    private String id;

    private String symbol;       // e.g. "AAPL"
    private String name;         // e.g. "Apple Inc."
    private double currentPrice;
    private double openPrice;
    private double high;
    private double low;
    private long volume;
    private String sector;

    private List<PricePoint> priceHistory = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricePoint {
        private double price;
        private LocalDateTime timestamp;
    }

    public double getChangePercent() {
        if (openPrice == 0) return 0;
        return ((currentPrice - openPrice) / openPrice) * 100;
    }

    public double getChange() {
        return currentPrice - openPrice;
    }
}
