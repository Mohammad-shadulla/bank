package com.shadulla.bank.service;

import com.shadulla.bank.entity.Account;
import com.shadulla.bank.exception.BankException;
import com.shadulla.bank.repository.AccountRepository;
import com.shadulla.bank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BankServiceTest {

    private AccountRepository accounts;
    private TransactionRepository transactions;
    private BankService service;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepository.class);
        transactions = mock(TransactionRepository.class);
        service = new BankService(accounts, transactions);
        // save() returns the same account it receives
        when(accounts.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Account account(String number, String owner, String balance) {
        Account a = new Account();
        a.setAccountNumber(number);
        a.setOwner(owner);
        a.setBalance(new BigDecimal(balance));
        when(accounts.findByAccountNumber(number)).thenReturn(Optional.of(a));
        return a;
    }

    @Test
    void createAccount_blankName_throws() {
        assertThrows(BankException.class, () -> service.createAccount(" ", "user1"));
    }

    @Test
    void deposit_addsToBalance() {
        account("AC1", "user1", "100");
        Account result = service.deposit("AC1", new BigDecimal("50"), "user1");
        assertEquals(new BigDecimal("150"), result.getBalance());
    }

    @Test
    void deposit_negativeAmount_throws() {
        account("AC1", "user1", "100");
        assertThrows(BankException.class,
                () -> service.deposit("AC1", new BigDecimal("-5"), "user1"));
    }

    @Test
    void withdraw_moreThanBalance_throws() {
        account("AC1", "user1", "100");
        assertThrows(BankException.class,
                () -> service.withdraw("AC1", new BigDecimal("500"), "user1"));
    }

    @Test
    void withdraw_reducesBalance() {
        account("AC1", "user1", "100");
        Account result = service.withdraw("AC1", new BigDecimal("40"), "user1");
        assertEquals(new BigDecimal("60"), result.getBalance());
    }

    @Test
    void transfer_sameAccount_throws() {
        account("AC1", "user1", "100");
        assertThrows(BankException.class,
                () -> service.transfer("AC1", "AC1", new BigDecimal("10"), "user1"));
    }

    @Test
    void transfer_movesMoneyBetweenAccounts() {
        Account sender = account("AC1", "user1", "100");
        Account receiver = account("AC2", "user2", "20");

        service.transfer("AC1", "AC2", new BigDecimal("30"), "user1");

        assertEquals(new BigDecimal("70"), sender.getBalance());
        assertEquals(new BigDecimal("50"), receiver.getBalance());
    }

    @Test
    void transfer_fromSomeoneElsesAccount_throws() {
        account("AC1", "user1", "100");
        account("AC2", "user2", "20");
        assertThrows(BankException.class,
                () -> service.transfer("AC1", "AC2", new BigDecimal("10"), "user2"));
    }

    @Test
    void getOwnedAccount_otherUser_throws() {
        account("AC1", "user1", "100");
        assertThrows(BankException.class, () -> service.getOwnedAccount("AC1", "user2"));
    }

    @Test
    void deleteAccount_withMoney_throws() {
        account("AC1", "user1", "100");
        assertThrows(BankException.class, () -> service.deleteAccount("AC1", "user1"));
    }
}