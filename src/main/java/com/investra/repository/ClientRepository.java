package com.investra.repository;

import com.investra.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByNationalityNumber(String nationalityNumber);

    Optional<Client> findByTaxId(String taxId);

    // Mavi Kart Numarası ile arama
    Optional<Client> findByBlueCardNo(String blueCardNo);
}
