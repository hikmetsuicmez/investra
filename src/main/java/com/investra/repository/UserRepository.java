package com.investra.repository;

import com.investra.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNationalityNumber(String nationalityNumber);

    Optional<User> findByEmployeeNumber(String employeeNumber);

    Optional<User> findByPasswordResetToken(String token);

    Optional<User> findTopByOrderByEmployeeNumberDesc();

    Optional<User> findByUsername(String username);
}
