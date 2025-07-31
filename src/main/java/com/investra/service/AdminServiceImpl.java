package com.investra.service;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.UpdateUserResponse;
import com.investra.entity.User;
import com.investra.repository.UserRepository;
import com.investra.utils.PasswordGenerator;
import com.investra.utils.EmployeeNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static com.investra.utils.AdminOperationsValidator.duplicateResourceCheck;
import static com.investra.mapper.UserMapper.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeNumberGenerator employeeNumberGenerator;
    @Override
    public Response<CreateUserResponse> createUser(CreateUserRequest request) {
        String generatedSicilNo = employeeNumberGenerator.generateNext();
        try {
            duplicateResourceCheck(() -> userRepository.findByNationalityNumber(request.getNationalityNumber()).isPresent(), "Bu TCKN ile kayıtlı bir kullanıcı  mevcut");
            duplicateResourceCheck(() -> userRepository.findByEmail(request.getEmail()).isPresent(), "Bu email ile kayıtlı bir kullanıcı  mevcut");
            String rawPassword = PasswordGenerator.generatePassword(10);
            String encodedPassword = passwordEncoder.encode(rawPassword);

            User user = toEntity(request, encodedPassword, generatedSicilNo);
            userRepository.save(user);

            CreateUserResponse response = toResponse(request, rawPassword,generatedSicilNo);

            return Response.<CreateUserResponse>builder()
                    .statusCode(201)
                    .message("Personel başarıyla eklendi")
                    .data(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.<CreateUserResponse>builder()
                    .statusCode(400)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public Response<UpdateUserResponse> updateUser(String sicilNo, UpdateUserRequest request) {
        try {
            User user = userRepository.findByEmployeeNumber(sicilNo)
                    .orElseThrow(() -> new IllegalArgumentException("Güncellenecek kullanıcı bulunamadı"));

            updateFields(user, request);
            userRepository.save(user);
            UpdateUserResponse response = toUpdateResponse(user);
            return Response.<UpdateUserResponse>builder()
                    .statusCode(200)
                    .message("Kullanıcı bilgileri güncellendi")
                    .data(response)
                    .build();

        } catch (IllegalArgumentException e) {
            return Response.<UpdateUserResponse>builder()
                    .statusCode(404)
                    .message(e.getMessage())
                    .build();
        }
    }


}
