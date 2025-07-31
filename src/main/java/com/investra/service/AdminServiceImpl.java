package com.investra.service;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.User;
import com.investra.repository.UserRepository;
import com.investra.utils.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static com.investra.utils.AdminOperationsValidator.duplicateResourceCheck;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Response<CreateUserResponse> createUser(CreateUserRequest request) {
        try {
            duplicateResourceCheck(() -> userRepository.findByTckn(request.getTckn()).isPresent(), "Bu TCKN ile kayıtlı bir kullanıcı  mevcut");
            duplicateResourceCheck(() -> userRepository.findByEmail(request.getEmail()).isPresent(), "Bu email ile kayıtlı bir kullanıcı  mevcut");
            String rawPassword = PasswordGenerator.generatePassword(10);
            String encodedPassword = passwordEncoder.encode(rawPassword);

            User user = mapToUserEntity(request, encodedPassword);
            userRepository.save(user);

            CreateUserResponse response = mapToUserResponse(request,rawPassword);

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

    private User mapToUserEntity(CreateUserRequest request, String encodedPassword) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .tckn(request.getTckn())
                .sicilNo(request.getSicilNo())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .role(request.getRole())
                .password(encodedPassword)
                .build();
    }

    private CreateUserResponse mapToUserResponse(CreateUserRequest request,String password) {
        return CreateUserResponse.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .tckn(request.getTckn())
                .sicilNo(request.getSicilNo())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .role(request.getRole().name())
                .password(password)
                .build();
    }

}
