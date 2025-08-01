package com.investra.service;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.NotificationDTO;
import com.investra.dtos.response.Response;
import com.investra.entity.User;
import com.investra.enums.NotificationType;
import com.investra.repository.UserRepository;
import com.investra.utils.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    @Override
    public Response<CreateUserResponse> createUser(CreateUserRequest request) {
        try {
            duplicateResourceCheck(() -> userRepository.findByTckn(request.getTckn()).isPresent(), "Bu TCKN ile kayıtlı bir kullanıcı  mevcut");
            duplicateResourceCheck(() -> userRepository.findByEmail(request.getEmail()).isPresent(), "Bu email ile kayıtlı bir kullanıcı  mevcut");
            String rawPassword = PasswordGenerator.generatePassword(10);
            String encodedPassword = passwordEncoder.encode(rawPassword);

            User user = toEntity(request, encodedPassword);
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

            CreateUserResponse response = toResponse(request, rawPassword);

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


}
