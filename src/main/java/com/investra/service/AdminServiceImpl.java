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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    @Override
    public Response<CreateUserResponse> createUser(CreateUserRequest request) {
        try {
            duplicateResourceCheck(() -> userRepository.findByTckn(request.getTckn()).isPresent(), "Bu TCKN ile kayıtlı bir kullanıcı  mevcut");
            duplicateResourceCheck(() -> userRepository.findByEmail(request.getEmail()).isPresent(), "Bu email ile kayıtlı bir kullanıcı  mevcut");
            String rawPassword = PasswordGenerator.generatePassword(10);
            String encodedPassword = passwordEncoder.encode(rawPassword);

            User user = toEntity(request, encodedPassword);
            userRepository.save(user);

            String emailContent = "<h2>Merhaba " + request.getFirstName() + ",</h2>" +
                    "<p>Yeni hesabınız başarıyla oluşturuldu. Aşağıdaki bilgilerle giriş yapabilirsiniz:</p>" +
                    "<p><strong>Email:</strong> " + user.getEmail() + "</p>" +
                    "<p><strong>Şifre:</strong> " + rawPassword + "</p>" +
                    "<p>Giriş yapmak için <a href=\"" + FRONTEND_URL + "/login\">buraya tıklayın</a>.</p>" +
                    "<p>Teşekkürler,</p>" +
                    "<p>Investra Ekibi</p>" +
                    "<p>Not: Bu e-posta otomatik olarak oluşturulmuştur, lütfen yanıtlamayın.</p>";

            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject("Yeni Hesap Oluşturma")
                    .content(emailContent)
                    .type(NotificationType.SUCCESS)
                    .isHtml(true)
                    .build();

            try {
                notificationService.sendEmail(notificationDTO);
                System.out.println("Email başarıyla gönderildi");
            } catch (Exception e) {
                System.err.println("Email gönderilirken hata oluştu: " + e.getMessage());
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
