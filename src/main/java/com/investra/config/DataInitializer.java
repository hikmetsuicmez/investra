package com.investra.config;

import com.investra.entity.User;
import com.investra.enums.Role;
import com.investra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
            User adminUser = User.builder()
                    .email("admin@investra.com")
                    .password(passwordEncoder.encode("123456"))
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.ADMIN)
                    .isActive(true)
                    .firstLogin(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            User traderUser = User.builder()
                    .email("trader@investra.com")
                    .password(passwordEncoder.encode("123456"))
                    .firstName("Trader")
                    .lastName("User")
                    .role(Role.TRADER)
                    .isActive(true)
                    .firstLogin(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(adminUser);
            userRepository.save(traderUser);
            System.out.println("Test admin kullanıcısı oluşturuldu: admin@investra.com / 123456");
            System.out.println("Test admin kullanıcısı oluşturuldu: trader@investra.com / 123456");
    }
}
