package com.stocktrading.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    private String userId;
    private String stockSymbol;
    private String stockName;
    private TransactionType type;  // BUY or SELL
    private int quantity;
    private double pricePerShare;
    private double totalAmount;
    private LocalDateTime timestamp;

    public enum TransactionType {
        BUY, SELL
    }
}
