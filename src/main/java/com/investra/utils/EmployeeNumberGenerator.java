package com.investra.utils;

import com.investra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeNumberGenerator {
    private final UserRepository userRepository;

    public String generateNext() {
        return userRepository.findTopByOrderByNationalityNumberDesc()
                .map(user -> String.format("%04d", Integer.parseInt(user.getEmployeeNumber()) + 1))
                .orElse("0001");
    }

}
