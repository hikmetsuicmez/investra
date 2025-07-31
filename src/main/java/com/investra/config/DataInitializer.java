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
        userRepository.deleteAll();
            User adminUser = User.builder()
                    .email("admin@investra.com")
                    .password(passwordEncoder.encode("12345678"))
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.ADMIN)
                    .isActive(true)
                    .firstLogin(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            User traderUser = User.builder()
                    .email("trader@investra.com")
                    .password(passwordEncoder.encode("12345678"))
                    .firstName("Trader")
                    .lastName("User")
                    .role(Role.TRADER)
                    .isActive(true)
                    .firstLogin(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            User hikmet = User.builder()
                    .email("suicmezhikmet1@gmail.com")
                    .password(passwordEncoder.encode("12345678"))
                    .firstName("Hikmet")
                    .lastName("Suicmez")
                    .role(Role.ADMIN)
                    .isActive(true)
                    .firstLogin(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(adminUser);
            userRepository.save(traderUser);
            userRepository.save(hikmet);
            System.out.println("Test admin kullanıcısı oluşturuldu: admin@investra.com / 12345678");
            System.out.println("Test trader kullanıcısı oluşturuldu: trader@investra.com / 12345678");
            System.out.println("Test admin kullanıcısı oluşturuldu: suicmezhikmet1@gmail.com / 12345678");

    }
}
