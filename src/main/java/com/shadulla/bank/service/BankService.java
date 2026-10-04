package com.shadulla.bank.service;

import com.shadulla.bank.entity.Account;
import com.shadulla.bank.entity.TransactionRecord;
import com.shadulla.bank.exception.BankException;
import com.shadulla.bank.repository.AccountRepository;
import com.shadulla.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankService {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public BankService(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public Account createAccount(String holderName) {
        if (holderName == null || holderName.isBlank()) {
            throw new BankException("Holder name is required");
        }
        Account a = new Account();
        a.setHolderName(holderName);
        a.setAccountNumber("AC" + System.currentTimeMillis());
        return accounts.save(a);
    }

    public Account getAccount(String number) {
        return accounts.findByAccountNumber(number)
                .orElseThrow(() -> new BankException("Account not found: " + number));
    }

    @Transactional
    public Account deposit(String number, BigDecimal amount) {
        checkAmount(amount);
        Account a = getAccount(number);
        a.setBalance(a.getBalance().add(amount));
        saveRecord("DEPOSIT", null, number, amount);
        return accounts.save(a);
    }

    @Transactional
    public Account withdraw(String number, BigDecimal amount) {
        checkAmount(amount);
        Account a = getAccount(number);
        if (a.getBalance().compareTo(amount) < 0) {
            throw new BankException("Insufficient balance");
        }
        a.setBalance(a.getBalance().subtract(amount));
        saveRecord("WITHDRAW", number, null, amount);
        return accounts.save(a);
    }

    @Transactional
    public void transfer(String from, String to, BigDecimal amount) {
        checkAmount(amount);
        if (from == null || to == null || from.equals(to)) {
            throw new BankException("Choose two different accounts");
        }

        Account sender = getAccount(from);
        Account receiver = getAccount(to);

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new BankException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accounts.save(sender);
        accounts.save(receiver);
        saveRecord("TRANSFER", from, to, amount);
    }

    public List<TransactionRecord> history(String number) {
        getAccount(number); // makes sure the account exists
        return transactions.findByFromAccountOrToAccountOrderByCreatedAtDesc(number, number);
    }

    private void checkAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Amount must be greater than 0");
        }
    }

    private void saveRecord(String type, String from, String to, BigDecimal amount) {
        TransactionRecord t = new TransactionRecord();
        t.setType(type);
        t.setFromAccount(from);
        t.setToAccount(to);
        t.setAmount(amount);
        transactions.save(t);
    }
}