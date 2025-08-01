package com.investra.repository;

import com.investra.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByTckn(String tckn);

    // Vergi Kimlik Numarası ile arama
    Optional<Client> findByVergiNo(String vergiNo);

    // Mavi Kart Numarası ile arama
    Optional<Client> findByBlueCardNo(String blueCardNo);
}
