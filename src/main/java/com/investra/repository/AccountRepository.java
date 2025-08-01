package com.investra.repository;

import com.investra.entity.Account;
import com.investra.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // Müşterinin tüm takas hesaplarını getir
    @Query("SELECT a FROM Account a WHERE a.client.id = :clientId AND a.accountType = 'TAKAS'")
    List<Account> findTakasAccountsByClientId(@Param("clientId") Long clientId);

    // Müşterinin birincil takas hesabını getir
    @Query("SELECT a FROM Account a WHERE a.client.id = :clientId AND a.accountType = 'TAKAS' AND a.isPrimaryTakas = true")
    Optional<Account> findPrimaryTakasAccountByClientId(@Param("clientId") Long clientId);

    // Hesap numarasına göre hesap getir
    Optional<Account> findByAccountNumber(String accountNumber);

    // Müşterinin tüm hesaplarını getir
    List<Account> findByClientId(Long clientId);

    // Müşterinin belirli tipteki hesaplarını getir
    List<Account> findByClientIdAndAccountType(Long clientId, AccountType accountType);
}
