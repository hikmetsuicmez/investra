package com.investra.service;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.*;
import com.investra.entity.User;
import com.investra.enums.NotificationType;
import com.investra.exception.UserNotFoundException;
import com.investra.mapper.UserMapper;
import com.investra.repository.UserRepository;
import com.investra.utils.PasswordGenerator;
import com.investra.utils.EmployeeNumberGenerator;
import jakarta.validation.constraints.AssertFalse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
        log.info("createUser çağrıldı. employeeNumber: {}, email: {}, tckn: {}",
                generatedEmployeeNumber, request.getEmail(), request.getNationalityNumber());

        try {
            log.debug("TCKN kontrolü yapılıyor");
            duplicateResourceCheck(() -> userRepository.findByNationalityNumber(request.getNationalityNumber()).isPresent(),
                    "Bu TCKN ile kayıtlı bir kullanıcı mevcut");

            log.debug("Email kontrolü yapılıyor");
            duplicateResourceCheck(() -> userRepository.findByEmail(request.getEmail()).isPresent(),
                    "Bu email ile kayıtlı bir kullanıcı mevcut");

            log.debug("Şifre oluşturuluyor");
            String rawPassword = PasswordGenerator.generatePassword(10);
            String encodedPassword = passwordEncoder.encode(rawPassword);

            User user = toEntity(request, encodedPassword, generatedEmployeeNumber);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Yeni kullanıcı veritabanına kaydedildi. employeeNumber: {}", generatedEmployeeNumber);

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

            log.info("Kullanıcı başarıyla oluşturuldu. employeeNumber: {}", generatedEmployeeNumber);
            return Response.<CreateUserResponse>builder()
                    .statusCode(201)
                    .message("Kullanıcı başarıyla eklendi")
                    .data(response)
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("Kullanıcı oluşturulamadı: {}", e.getMessage());
            return Response.<CreateUserResponse>builder()
                    .statusCode(400)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Beklenmeyen bir hata oluştu: {}", e.getMessage(), e);
            return Response.<CreateUserResponse>builder()
                    .statusCode(500)
                    .message("Beklenmeyen bir hata oluştu")
                    .build();
        }
    }

    @Override
    public Response<UpdateUserResponse> updateUser(String employeeNumber, UpdateUserRequest request) {
        log.info("updateUser çağrıldı. employeeNumber: {}, request: {}", employeeNumber, request);

        try {
            User user = userRepository.findByEmployeeNumber(employeeNumber)
                    .orElseThrow(() -> {
                        String msg = "Güncellenecek kullanıcı bulunamadı. employeeNumber: " + employeeNumber;
                        log.warn(msg);
                        return new IllegalArgumentException(msg);
                    });
            log.info("Kullanıcı bulundu. employeeNumber: {} - Güncelleme başlatılıyor", employeeNumber);

            updateFields(user, request);
            userRepository.save(user);
            log.info("Kullanıcı veritabanına kaydedildi. employeeNumber: {}", employeeNumber);

            UpdateUserResponse response = toUpdateResponse(user);
            log.info("Kullanıcı başarıyla güncellendi. employeeNumber: {}", employeeNumber);

            return Response.<UpdateUserResponse>builder()
                    .statusCode(200)
                    .message("Kullanıcı bilgileri güncellendi")
                    .data(response)
                    .build();

        } catch (IllegalArgumentException e) {
            log.error("Kullanıcı güncellenemedi: {}", e.getMessage());
            return Response.<UpdateUserResponse>builder()
                    .statusCode(404)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Beklenmeyen bir hata oluştu. employeeNumber: {}, hata: {}", employeeNumber, e.getMessage(), e);
            return Response.<UpdateUserResponse>builder()
                    .statusCode(500)
                    .message("Beklenmeyen bir hata oluştu")
                    .build();
        }
    }

    @Override
    public Response<Void> deleteUser(String employeeNumber) {
        log.info("deleteUser çağrıldı. employeeNumber: {}", employeeNumber);

        try {
            User user = userRepository.findByEmployeeNumber(employeeNumber)
                    .orElseThrow(() -> {
                        String msg = "Kullanıcı bulunamadı. employeeNumber: " + employeeNumber;
                        log.warn(msg);
                        return new IllegalArgumentException(msg);
                    });

            if (!user.isActive()) {
                log.info("Kullanıcı zaten pasif durumda. employeeNumber: {}", employeeNumber);
                return Response.<Void>builder()
                        .statusCode(400)
                        .message("Kullanıcı pasif durumda")
                        .build();
            }

            user.setActive(false);
            userRepository.save(user);
            log.info("Kullanıcı başarıyla pasif hale getirildi. employeeNumber: {}", employeeNumber);

            return Response.<Void>builder()
                    .statusCode(200)
                    .message("Kullanıcı pasif hale getirildi")
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("Kullanıcı pasifleştirme sırasında hata oluştu: {}", e.getMessage());
            return Response.<Void>builder()
                    .statusCode(404)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Bilinmeyen bir hata oluştu. employeeNumber: {}, hata: {}", employeeNumber, e.getMessage(), e);
            return Response.<Void>builder()
                    .statusCode(500)
                    .message("Beklenmeyen bir hata oluştu")
                    .build();
        }
    }

    @Override
    public Response<List<UserDTO>> retrieveAllUsers() {
        List<User> users = userRepository.findAll();
        log.info("retrieveAllUsers çağrıldı. Toplam kullanıcı sayısı: {}", users.size());
        if (users.isEmpty()) {
            log.info("Kullanıcı bulunamadı");
            return Response.<List<UserDTO>>builder()
                    .statusCode(404)
                    .message("Kullanıcı bulunamadı")
                    .data(List.of())
                    .build();
        }
        log.info("Kullanıcılar başarıyla alındı. Toplam kullanıcı sayısı: {}", users.size());
        List<UserDTO> userDTOS = users.stream()
                .map(UserMapper::toUserDTO)
                .toList();

        log.info("Kullanıcı DTO'ları başarıyla oluşturuldu. Toplam kullanıcı sayısı: {}", userDTOS.size());

        return Response.<List<UserDTO>>builder()
                .statusCode(200)
                .message("Kullanıcılar başarıyla alındı")
                .data(userDTOS)
                .build();
    }

    @Override
    public Response<UserDTO> retrieveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Kullanıcı bulunamadı: {}", userId);
                    return new UserNotFoundException(userId);
                });

        UserDTO userDTO = UserMapper.toUserDTO(user);
        return Response.<UserDTO>builder()
                .statusCode(200)
                .message("Kullanıcı başarıyla alındı")
                .data(userDTO)
                .build();
    }

}
