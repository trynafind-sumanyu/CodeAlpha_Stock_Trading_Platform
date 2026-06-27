package com.stocktrading.service;

import com.stocktrading.model.Stock;
import com.stocktrading.model.Transaction;
import com.stocktrading.model.User;
import com.stocktrading.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradingService {

    private final StockService stockService;
    private final UserService userService;
    private final TransactionRepository transactionRepository;

    public Transaction buyStock(String userId, String symbol, int quantity) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stock stock = stockService.getStockBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));

        double totalCost = stock.getCurrentPrice() * quantity;
        if (user.getBalance() < totalCost) {
            throw new RuntimeException("Insufficient balance. Need $" + String.format("%.2f", totalCost)
                    + " but have $" + String.format("%.2f", user.getBalance()));
        }

        // Deduct balance
        user.setBalance(user.getBalance() - totalCost);

        // Update portfolio
        int currentQty = user.getPortfolio().getOrDefault(symbol, 0);
        double currentAvg = user.getAvgBuyPrice().getOrDefault(symbol, 0.0);

        // Weighted average buy price
        double newAvg = ((currentAvg * currentQty) + (stock.getCurrentPrice() * quantity))
                / (currentQty + quantity);

        user.getPortfolio().put(symbol, currentQty + quantity);
        user.getAvgBuyPrice().put(symbol, newAvg);
        userService.saveUser(user);

        // Record transaction
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setStockSymbol(symbol);
        tx.setStockName(stock.getName());
        tx.setType(Transaction.TransactionType.BUY);
        tx.setQuantity(quantity);
        tx.setPricePerShare(stock.getCurrentPrice());
        tx.setTotalAmount(totalCost);
        tx.setTimestamp(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    public Transaction sellStock(String userId, String symbol, int quantity) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stock stock = stockService.getStockBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));

        int currentQty = user.getPortfolio().getOrDefault(symbol, 0);
        if (currentQty < quantity) {
            throw new RuntimeException("Insufficient shares. You own " + currentQty + " shares of " + symbol);
        }

        double totalRevenue = stock.getCurrentPrice() * quantity;

        // Update balance
        user.setBalance(user.getBalance() + totalRevenue);

        // Update portfolio
        int remaining = currentQty - quantity;
        if (remaining == 0) {
            user.getPortfolio().remove(symbol);
            user.getAvgBuyPrice().remove(symbol);
        } else {
            user.getPortfolio().put(symbol, remaining);
        }
        userService.saveUser(user);

        // Record transaction
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setStockSymbol(symbol);
        tx.setStockName(stock.getName());
        tx.setType(Transaction.TransactionType.SELL);
        tx.setQuantity(quantity);
        tx.setPricePerShare(stock.getCurrentPrice());
        tx.setTotalAmount(totalRevenue);
        tx.setTimestamp(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    public List<Transaction> getUserTransactions(String userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public Map<String, Object> getPortfolioSummary(String userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Stock> stockMap = stockService.getAllStocks().stream()
                .collect(Collectors.toMap(Stock::getSymbol, s -> s));

        double portfolioValue = user.getCurrentPortfolioValue(stockMap);
        double totalInvested = user.getTotalInvested(stockMap);
        double pnl = portfolioValue - totalInvested;
        double pnlPercent = totalInvested == 0 ? 0 : (pnl / totalInvested) * 100;

        // Build holdings detail
        List<Map<String, Object>> holdings = user.getPortfolio().entrySet().stream()
                .map(entry -> {
                    String sym = entry.getKey();
                    int qty = entry.getValue();
                    Stock stock = stockMap.get(sym);
                    double avg = user.getAvgBuyPrice().getOrDefault(sym, 0.0);
                    double currentVal = stock != null ? stock.getCurrentPrice() * qty : 0;
                    double invested = avg * qty;
                    double gainLoss = currentVal - invested;
                    return Map.<String, Object>of(
                        "symbol", sym,
                        "name", stock != null ? stock.getName() : sym,
                        "quantity", qty,
                        "avgBuyPrice", avg,
                        "currentPrice", stock != null ? stock.getCurrentPrice() : 0,
                        "currentValue", currentVal,
                        "gainLoss", gainLoss,
                        "gainLossPercent", invested == 0 ? 0 : (gainLoss / invested) * 100
                    );
                })
                .collect(Collectors.toList());

        return Map.of(
            "userId", userId,
            "username", user.getUsername(),
            "cashBalance", user.getBalance(),
            "portfolioValue", portfolioValue,
            "totalValue", user.getBalance() + portfolioValue,
            "totalInvested", totalInvested,
            "pnl", pnl,
            "pnlPercent", pnlPercent,
            "holdings", holdings
        );
    }
}
