package com.investra.service.helper;

import com.investra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeNumberGenerator {
    private final UserRepository userRepository;

    public synchronized String generateNext() {
        // En yüksek employee number'ı bul
        Optional<String> topEmployeeNumber = userRepository.findTopByOrderByEmployeeNumberDesc()
                .map(user -> user.getEmployeeNumber());

        if (topEmployeeNumber.isEmpty()) {
            log.info("Veritabanında hiç kullanıcı bulunamadı, ilk employee number: 0001");
            return "0001";
        }

        String empNo = topEmployeeNumber.get();
        if (empNo == null || !empNo.matches("\\d+")) {
            log.warn("Geçersiz employee number formatı: {}, 0001 döndürülüyor", empNo);
            return "0001";
        }

        // Mevcut employee number'ları kontrol ederek benzersiz bir değer bul
        int startNumber = Integer.parseInt(empNo) + 1;
        int maxAttempts = 100; // Maksimum 100 deneme

        for (int i = 0; i < maxAttempts; i++) {
            int candidateNumber = startNumber + i;
            String candidateEmployeeNumber = String.format("%04d", candidateNumber);

            // Bu employee number zaten kullanımda mı kontrol et
            if (!userRepository.findByEmployeeNumber(candidateEmployeeNumber).isPresent()) {
                log.info("Benzersiz employee number bulundu: {} -> {}", empNo, candidateEmployeeNumber);
                return candidateEmployeeNumber;
            }

            log.debug("Employee number {} zaten kullanımda, bir sonrakini deniyorum", candidateEmployeeNumber);
        }

        // Eğer hiç benzersiz değer bulunamazsa, çok yüksek bir sayı döndür
        String fallbackNumber = String.format("%04d", startNumber + maxAttempts);
        log.warn("Benzersiz employee number bulunamadı, fallback değer döndürülüyor: {}", fallbackNumber);
        return fallbackNumber;
    }
}
