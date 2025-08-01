package com.investra.repository;

import com.investra.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    // T.C. Kimlik Numarası ile arama
    Optional<Client> findByTckn(String tckn);

    // Vergi Kimlik Numarası ile arama
    Optional<Client> findByVergiNo(String vergiNo);

    // Mavi Kart Numarası ile arama
    Optional<Client> findByBlueCardNo(String blueCardNo);

    // Müşteri ismi ile arama (case-insensitive)
    @Query("SELECT c FROM Client c WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Client> findByFullNameContainingIgnoreCase(@Param("name") String name);

    // Hesap numarası ile müşteriyi bulma
    @Query("SELECT c FROM Client c JOIN c.portfolios p JOIN Account a ON a.client.id = c.id WHERE a.accountNumber = :accountNumber")
    Optional<Client> findByAccountNumber(@Param("accountNumber") String accountNumber);

    // Genel arama - tüm kriterler için
    @Query("SELECT DISTINCT c FROM Client c LEFT JOIN Account a ON a.client.id = c.id " +
           "WHERE c.tckn = :searchTerm OR c.vergiNo = :searchTerm OR c.blueCardNo = :searchTerm " +
           "OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR a.accountNumber = :searchTerm")
    List<Client> searchClients(@Param("searchTerm") String searchTerm);
}
