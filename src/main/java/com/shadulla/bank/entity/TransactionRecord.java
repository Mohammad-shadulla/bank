package com.shadulla.bank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;          // DEPOSIT, WITHDRAW, TRANSFER
    private String fromAccount;
    private String toAccount;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    private LocalDateTime createdAt = LocalDateTime.now();
}