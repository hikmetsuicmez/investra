package com.investra.repository;

import com.investra.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByNationalityNumber(String nationalityNumber);

    Optional<Client> findByTaxId(String taxId);

    // Mavi Kart Numarası ile arama
    Optional<Client> findByBlueCardNo(String blueCardNo);
    Optional<Client> findByEmail(String email);
    Optional<Client> findByPassportNo(String passportNo);
    Optional<Client> findByTaxNumber(String taxNumber);
    Optional<Client> findFirstByNationalityNumberOrPassportNoOrBlueCardNoOrTaxNumberOrRegistrationNumber(
            String nationalityNumber,
            String passportNo,
            String blueCardNo,
            String taxNumber,
            String registrationNumber
    );


}
