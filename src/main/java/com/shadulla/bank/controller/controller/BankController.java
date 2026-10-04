package com.shadulla.bank.controller;

import com.shadulla.bank.entity.Account;
import com.shadulla.bank.entity.TransactionRecord;
import com.shadulla.bank.service.BankService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BankController {

    private final BankService service;

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
    public Map<String, String> transfer(@RequestBody TransferRequest r) {
        service.transfer(r.from(), r.to(), r.amount());
        return Map.of("message", "Transfer successful");
    }

    @GetMapping("/accounts/{number}/history")
    public List<TransactionRecord> history(@PathVariable String number) {
        return service.history(number);
    }
}