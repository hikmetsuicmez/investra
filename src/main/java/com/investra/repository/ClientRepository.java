package com.investra.repository;

import com.investra.entity.Client;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByNationalityNumber(String nationalityNumber);

    Optional<Client> findByTaxId(String taxId);

    // Mavi Kart Numarası ile arama
    Optional<Client> findByBlueCardNo(String blueCardNo);

    @Query("SELECT c FROM Client c WHERE LOWER(c.fullName) = LOWER(:name)")
    Optional<Client> findByName(@Param("name") String name);

}
