package com.investra.repository;

import com.investra.entity.Account;
import com.investra.enums.AccountType;
import com.investra.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {

    Optional<Account> findByClientId(Long clientId);

    List<Account> findByClientIdOrderByCreatedAtDesc(Long clientId);

    Optional<Account> findByIban(String iban);

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumberAtBroker(String accountNumberAtBroker);

    @Query("SELECT a FROM Account a WHERE a.client.id = :clientId AND a.accountType = :accountType AND a.currency = :currency")
    List<Account> findByClientIdAndAccountTypeAndCurrency(
            @Param("clientId") Long clientId,
            @Param("accountType") AccountType accountType,
            @Param("currency") Currency currency);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.client.id = :clientId")
    int countAccountsByClientId(@Param("clientId") Long clientId);

    boolean existsByIban(String iban);

    boolean existsByAccountNumberAtBroker(String accountNumberAtBroker);
}