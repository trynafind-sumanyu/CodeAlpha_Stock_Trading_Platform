package com.stocktrading.controller;

import com.stocktrading.model.Transaction;
import com.stocktrading.service.TradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TradingController {

    private final TradingService tradingService;

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody Map<String, Object> body) {
        try {
            String userId = (String) body.get("userId");
            String symbol = ((String) body.get("symbol")).toUpperCase();
            int quantity = Integer.parseInt(body.get("quantity").toString());
            Transaction tx = tradingService.buyStock(userId, symbol, quantity);
            return ResponseEntity.ok(tx);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sell(@RequestBody Map<String, Object> body) {
        try {
            String userId = (String) body.get("userId");
            String symbol = ((String) body.get("symbol")).toUpperCase();
            int quantity = Integer.parseInt(body.get("quantity").toString());
            Transaction tx = tradingService.sellStock(userId, symbol, quantity);
            return ResponseEntity.ok(tx);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/portfolio/{userId}")
    public ResponseEntity<?> portfolio(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(tradingService.getPortfolioSummary(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/transactions/{userId}")
    public List<Transaction> transactions(@PathVariable String userId) {
        return tradingService.getUserTransactions(userId);
    }
}
