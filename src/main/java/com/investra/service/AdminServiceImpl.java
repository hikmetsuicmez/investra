package com.investra.service;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.NotificationDTO;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.UpdateUserResponse;
import com.investra.entity.User;
import com.investra.enums.NotificationType;
import com.investra.repository.UserRepository;
import com.investra.utils.PasswordGenerator;
import com.investra.utils.EmployeeNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.investra.utils.AdminOperationsValidator.duplicateResourceCheck;
import static com.investra.mapper.UserMapper.*;
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Value("${app.frontendUrl}")
    private String FRONTEND_URL;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeNumberGenerator employeeNumberGenerator;
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    @Override
    public Response<CreateUserResponse> createUser(CreateUserRequest request) {
        String generatedEmployeeNumber = employeeNumberGenerator.generateNext();
        try {
            duplicateResourceCheck(() -> userRepository.findByNationalityNumber(request.getNationalityNumber()).isPresent(), "Bu TCKN ile kayıtlı bir kullanıcı mevcut");
            duplicateResourceCheck(() -> userRepository.findByEmail(request.getEmail()).isPresent(), "Bu email ile kayıtlı bir kullanıcı mevcut");
            String rawPassword = PasswordGenerator.generatePassword(10);
            String encodedPassword = passwordEncoder.encode(rawPassword);

            User user = toEntity(request, encodedPassword, generatedEmployeeNumber);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);

            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("title", "Investra'ya Hoş Geldiniz!");
            templateVariables.put("userName", user.getFirstName() + " " + user.getLastName());
            templateVariables.put("welcomeMessage", "Investra ailesine katıldığınız için teşekkür ederiz. Hesabınız başarıyla oluşturulmuştur.");
            templateVariables.put("email", user.getEmail());
            templateVariables.put("password", rawPassword);
            templateVariables.put("loginUrl", FRONTEND_URL + "/auth/login");

            String emailContent = emailTemplateService.processTemplate("user-welcome", templateVariables);

            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject("Investra'ya Hoş Geldiniz!")
                    .content(emailContent)
                    .type(NotificationType.SUCCESS)
                    .isHtml(true)
                    .build();

            try {
                notificationService.sendEmail(notificationDTO);
                log.info("Email başarıyla gönderildi: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Email gönderilirken hata oluştu: {}", e.getMessage());
                throw new IllegalArgumentException("Email gönderilirken hata oluştu: " + e.getMessage());
            }

            CreateUserResponse response = toResponse(request, rawPassword, generatedEmployeeNumber);

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
    public Response<UpdateUserResponse> updateUser(String employeeNumber, UpdateUserRequest request) {
        try {
            User user = userRepository.findByEmployeeNumber(employeeNumber)
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

    @Override
    public Response<Void> deleteUser(String employeeNumber) {
        try {
            User user = userRepository.findByEmployeeNumber(employeeNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

            if (!user.isActive()) {
                return Response.<Void>builder()
                        .statusCode(400)
                        .message("Kullanıcı pasif durumda")
                        .build();
            }

            user.setActive(false);
            userRepository.save(user);

            return Response.<Void>builder()
                    .statusCode(200)
                    .message("Kullanıcı pasif hale getirildi")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.<Void>builder()
                    .statusCode(404)
                    .message(e.getMessage())
                    .build();
        }
    }




}
