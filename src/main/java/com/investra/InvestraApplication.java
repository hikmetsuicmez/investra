package com.investra;

import com.investra.entity.User;
import com.investra.enums.Role;
import com.investra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling // Zamanlanmış görevleri etkinleştir
@RequiredArgsConstructor
public class InvestraApplication implements CommandLineRunner {


	public static void main(String[] args) {
		SpringApplication.run(InvestraApplication.class, args);
	}

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;


	@Override
	public void run(String... args) throws Exception {

		if (userRepository.count() == 0) {
			// Varsayılan kullanıcıyı oluştur
			User admin = User.builder()
					.firstName("Admin")
					.lastName("User")
					.email("admin@investra.com")
					.password(passwordEncoder.encode("admin123"))
					.role(Role.ADMIN)
					.firstLogin(true)
					.phoneNumber("+1234567890")
					.nationalityNumber("1234567890")
					.isActive(true)
					.build();

			userRepository.save(admin);
		}

	}
}
