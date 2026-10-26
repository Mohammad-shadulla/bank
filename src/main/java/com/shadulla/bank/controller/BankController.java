package com.shadulla.bank.controller;

import com.shadulla.bank.entity.Account;
import com.shadulla.bank.entity.TransactionRecord;
import com.shadulla.bank.service.BankService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public Account create(@RequestBody CreateRequest r, Authentication auth) {
        return service.createAccount(r.holderName(), auth.getName());
    }

    @GetMapping("/accounts")
    public List<Account> myAccounts(Authentication auth) {
        return service.myAccounts(auth.getName());
    }

    @GetMapping("/accounts/{number}")
    public Account get(@PathVariable String number, Authentication auth) {
        return service.getOwnedAccount(number, auth.getName());
    }

    @PostMapping("/accounts/{number}/deposit")
    public Account deposit(@PathVariable String number, @RequestBody AmountRequest r, Authentication auth) {
        return service.deposit(number, r.amount(), auth.getName());
    }

    @PostMapping("/accounts/{number}/withdraw")
    public Account withdraw(@PathVariable String number, @RequestBody AmountRequest r, Authentication auth) {
        return service.withdraw(number, r.amount(), auth.getName());
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@RequestBody TransferRequest r, Authentication auth) {
        try {
            transferLimiter.executeRunnable(() ->
                    service.transfer(r.from(), r.to(), r.amount(), auth.getName()));
            return ResponseEntity.ok(Map.of("message", "Transfer successful"));
        } catch (RequestNotPermitted e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many transfer requests. Try again after 1 minute."));
        }
    }

    @GetMapping("/accounts/{number}/history")
    public List<TransactionRecord> history(@PathVariable String number, Authentication auth) {
        return service.history(number, auth.getName());
    }

    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {
        return Map.of("username", authentication.getName());
    }

    @GetMapping("/accounts/{number}/balance")
    public Map<String, Object> balance(@PathVariable String number, Authentication auth) {
        Account account = service.getOwnedAccount(number, auth.getName());
        return Map.of("accountNumber", number, "balance", account.getBalance());
    }

    @DeleteMapping("/accounts/{number}")
    public Map<String, String> delete(@PathVariable String number, Authentication auth) {
        service.deleteAccount(number, auth.getName());
        return Map.of("message", "Account deleted");
    }
    @GetMapping("/admin/accounts")
    public List<Account> allAccounts() {
        return service.allAccounts();
    }
}