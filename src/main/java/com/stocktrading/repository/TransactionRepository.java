package com.stocktrading.repository;

import com.stocktrading.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUserIdOrderByTimestampDesc(String userId);
    List<Transaction> findByStockSymbol(String symbol);
}
