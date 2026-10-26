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

    public Account createAccount(String holderName, String owner) {
        if (holderName == null || holderName.isBlank()) {
            throw new BankException("Holder name is required");
        }
        Account a = new Account();
        a.setHolderName(holderName);
        a.setOwner(owner);
        a.setAccountNumber("AC" + System.currentTimeMillis());
        return accounts.save(a);
    }

    public List<Account> myAccounts(String owner) {
        return accounts.findByOwner(owner);
    }

    public List<Account> allAccounts() {
        return accounts.findAll();
    }

    // used inside the service (no owner check)
    public Account getAccount(String number) {
        return accounts.findByAccountNumber(number)
                .orElseThrow(() -> new BankException("Account not found: " + number));
    }

    // used for user actions: only the owner can access the account
    public Account getOwnedAccount(String number, String owner) {
        Account a = getAccount(number);
        if (!owner.equals(a.getOwner())) {
            throw new BankException("Account not found: " + number);
        }
        return a;
    }

    @Transactional
    public Account deposit(String number, BigDecimal amount, String owner) {
        checkAmount(amount);
        Account a = getOwnedAccount(number, owner);
        a.setBalance(a.getBalance().add(amount));
        saveRecord("DEPOSIT", null, number, amount);
        return accounts.save(a);
    }

    @Transactional
    public Account withdraw(String number, BigDecimal amount, String owner) {
        checkAmount(amount);
        Account a = getOwnedAccount(number, owner);
        if (a.getBalance().compareTo(amount) < 0) {
            throw new BankException("Insufficient balance");
        }
        a.setBalance(a.getBalance().subtract(amount));
        saveRecord("WITHDRAW", number, null, amount);
        return accounts.save(a);
    }

    @Transactional
    public void transfer(String from, String to, BigDecimal amount, String owner) {
        checkAmount(amount);
        if (from == null || to == null || from.equals(to)) {
            throw new BankException("Choose two different accounts");
        }

        Account sender = getOwnedAccount(from, owner); // you can only send from your own account
        Account receiver = getAccount(to);             // you can send to anyone's account

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new BankException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accounts.save(sender);
        accounts.save(receiver);
        saveRecord("TRANSFER", from, to, amount);
    }

    public List<TransactionRecord> history(String number, String owner) {
        getOwnedAccount(number, owner); // makes sure the account exists and is yours
        return transactions.findByFromAccountOrToAccountOrderByCreatedAtDesc(number, number);
    }

    public void deleteAccount(String number, String owner) {
        Account a = getOwnedAccount(number, owner);
        if (a.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BankException("Balance must be 0 to delete the account");
        }
        accounts.delete(a);
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