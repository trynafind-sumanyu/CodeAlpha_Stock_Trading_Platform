package com.stocktrading.service;

import com.stocktrading.model.Stock;
import com.stocktrading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final Random random = new Random();

    @PostConstruct
    public void seedStocks() {
        if (stockRepository.count() == 0) {
            List<Stock> stocks = List.of(
                createStock("AAPL", "Apple Inc.", 189.50, "Technology"),
                createStock("GOOGL", "Alphabet Inc.", 175.20, "Technology"),
                createStock("MSFT", "Microsoft Corporation", 415.30, "Technology"),
                createStock("AMZN", "Amazon.com Inc.", 185.60, "Consumer Cyclical"),
                createStock("TSLA", "Tesla Inc.", 177.80, "Automotive"),
                createStock("NVDA", "NVIDIA Corporation", 875.40, "Technology"),
                createStock("META", "Meta Platforms Inc.", 505.10, "Technology"),
                createStock("NFLX", "Netflix Inc.", 628.90, "Communication"),
                createStock("JPM",  "JPMorgan Chase & Co.", 198.70, "Financial"),
                createStock("V",    "Visa Inc.", 276.50, "Financial")
            );
            stockRepository.saveAll(stocks);
        }
    }

    private Stock createStock(String symbol, String name, double price, String sector) {
        Stock s = new Stock();
        s.setSymbol(symbol);
        s.setName(name);
        s.setCurrentPrice(price);
        s.setOpenPrice(price);
        s.setHigh(price);
        s.setLow(price);
        s.setVolume((long)(random.nextInt(9000000) + 1000000));
        s.setSector(sector);
        s.getPriceHistory().add(new Stock.PricePoint(price, LocalDateTime.now()));
        return s;
    }

    // Simulate price changes every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void simulatePriceChanges() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            double change = (random.nextDouble() - 0.48) * stock.getCurrentPrice() * 0.02;
            double newPrice = Math.max(1.0, stock.getCurrentPrice() + change);
            newPrice = Math.round(newPrice * 100.0) / 100.0;

            stock.setCurrentPrice(newPrice);
            stock.setHigh(Math.max(stock.getHigh(), newPrice));
            stock.setLow(Math.min(stock.getLow(), newPrice));
            stock.setVolume(stock.getVolume() + random.nextInt(100000));

            // Keep last 50 price points
            List<Stock.PricePoint> history = stock.getPriceHistory();
            history.add(new Stock.PricePoint(newPrice, LocalDateTime.now()));
            if (history.size() > 50) history.remove(0);
        }
        stockRepository.saveAll(stocks);
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Optional<Stock> getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol);
    }
}
