package com.investra.utils;

import com.investra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeNumberGenerator {
    private final UserRepository userRepository;

    public String generateNext() {
        return userRepository.findTopByOrderByEmployeeNumberDesc()
                .map(user -> {
                    String empNo = user.getEmployeeNumber();
                    if (empNo == null || !empNo.matches("\\d+")) {
                        return "0001";
                    }
                    return String.format("%04d", Integer.parseInt(empNo) + 1);
                })
                .orElse("0001");
    }

}
