package com.investra.repository;

import com.investra.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByNationalityNumber(String nationalityNumber);

    Optional<Client> findByTaxId(String taxId);

    Optional<Client> findByBlueCardNo(String blueCardNo);

    Optional<Client> findByEmail(String email);

    Optional<Client> findByPassportNo(String passportNo);

    Optional<Client> findByTaxNumber(String taxNumber);

    Optional<Client> findByNationalityNumberContaining(String nationalityNumber);

    Optional<Client> findByTaxIdContaining(String taxId);

    Optional<Client> findByPassportNoContaining(String passportNo);

    Optional<Client> findByBlueCardNoContaining(String blueCardNo);

    List<Client> findTop20ByOrderByCreatedAtDesc();

    @Query("SELECT c FROM Client c ORDER BY c.createdAt DESC")
    List<Client> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);

    @Query("SELECT c FROM Client c WHERE LOWER(c.fullName) = LOWER(:name)")
    Optional<Client> findByName(@Param("name") String name);
}
