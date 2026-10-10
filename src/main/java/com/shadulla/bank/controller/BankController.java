package com.shadulla.bank.controller;

import com.shadulla.bank.entity.Account;
import com.shadulla.bank.entity.TransactionRecord;
import com.shadulla.bank.service.BankService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BankController {

    private final BankService service;

    // Rate limiter: only 5 transfers allowed per minute
    private final RateLimiter transferLimiter = RateLimiter.of("transfer",
            RateLimiterConfig.custom()
                    .limitForPeriod(5)
                    .limitRefreshPeriod(Duration.ofMinutes(1))
                    .timeoutDuration(Duration.ZERO)
                    .build());

    public BankController(BankService service) {
        this.service = service;
    }

    public record CreateRequest(String holderName) {}
    public record AmountRequest(BigDecimal amount) {}
    public record TransferRequest(String from, String to, BigDecimal amount) {}

    @PostMapping("/accounts")
    public Account create(@RequestBody CreateRequest r) {
        return service.createAccount(r.holderName());
    }

    @GetMapping("/accounts/{number}")
    public Account get(@PathVariable String number) {
        return service.getAccount(number);
    }

    @PostMapping("/accounts/{number}/deposit")
    public Account deposit(@PathVariable String number, @RequestBody AmountRequest r) {
        return service.deposit(number, r.amount());
    }

    @PostMapping("/accounts/{number}/withdraw")
    public Account withdraw(@PathVariable String number, @RequestBody AmountRequest r) {
        return service.withdraw(number, r.amount());
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@RequestBody TransferRequest r) {
        try {
            transferLimiter.executeRunnable(() ->
                    service.transfer(r.from(), r.to(), r.amount()));
            return ResponseEntity.ok(Map.of("message", "Transfer successful"));
        } catch (RequestNotPermitted e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many transfer requests. Try again after 1 minute."));
        }
    }

    @GetMapping("/accounts/{number}/history")
    public List<TransactionRecord> history(@PathVariable String number) {
        return service.history(number);
    }
}