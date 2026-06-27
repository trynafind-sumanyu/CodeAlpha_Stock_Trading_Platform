package com.stocktrading.repository;

import com.stocktrading.model.Stock;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface StockRepository extends MongoRepository<Stock, String> {
    Optional<Stock> findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);
}
