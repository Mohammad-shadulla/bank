package com.shadulla.bank.repository;

import com.shadulla.bank.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {

    List<TransactionRecord> findByFromAccountOrToAccountOrderByCreatedAtDesc(String from, String to);
}