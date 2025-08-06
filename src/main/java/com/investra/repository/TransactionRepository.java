package com.investra.repository;

import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.Transaction;
import com.investra.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount(Account account);

    List<Transaction> findByClient(Client client);

    List<Transaction> findByTransactionType(TransactionType transactionType);

    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);

    Page<Transaction> findByAccount(Account account, Pageable pageable);

    Page<Transaction> findByClient(Client client, Pageable pageable);

    Page<Transaction> findByAccountAndTransactionDateBetween(
            Account account, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Transaction> findByClientAndTransactionDateBetween(
            Client client, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
