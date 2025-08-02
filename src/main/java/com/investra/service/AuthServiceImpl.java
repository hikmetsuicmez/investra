package com.investra.service;

import com.investra.dtos.request.ChangePasswordRequest;
import com.investra.dtos.request.LoginRequest;
import com.investra.dtos.request.ResetPasswordRequest;
import com.investra.dtos.response.LoginResponse;
import com.investra.dtos.response.NotificationDTO;
import com.investra.dtos.response.Response;
import com.investra.entity.User;
import com.investra.enums.NotificationType;
import com.investra.exception.InvalidCredentialsException;
import com.investra.exception.InvalidOrExpiredTokenException;
import com.investra.exception.NotificationException;
import com.investra.exception.UserNotFoundException;
import com.investra.repository.UserRepository;
import com.investra.security.AuthUser;
import com.investra.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Value("${app.frontendUrl}")
    private String FRONTEND_URL;

    private static final String RESET_PASSWORD_URL = "/reset-password?token=";

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtUtil.generateToken(user.getEmail());

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setFirstLogin(user.isFirstLogin());

            return Response.<LoginResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Giriş başarılı")
                    .data(loginResponse)
                    .build();

        } catch (InvalidCredentialsException e) {
            return Response.<LoginResponse>builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("E-posta veya şifre hatalı")
                    .build();
        } catch (Exception e) {
            return Response.<LoginResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Giriş işlemi sırasında bir hata oluştu: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response<Void> changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        User user = userRepository.findByEmail(authUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException(authUser.getUsername()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Mevcut şifre hatalı");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Yeni şifre ve şifre tekrarı eşleşmiyor");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // İlk kez giriş yapmışsa, artık ilk giriş değil
        if (user.isFirstLogin()) {
            user.setFirstLogin(false);
        }
        userRepository.save(user);
        return Response.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Şifreniz başarıyla değiştirildi")
                .build();
    }

    @Override
    public Response<Void> forgotPassword(String email) {
        boolean emailExists = userRepository.findByEmail(email).isPresent();

        if (!emailExists) {
            log.info("Şifre sıfırlama isteği yapılan email bulunamadı: {}", email);
            throw new UserNotFoundException("Bu e-posta adresi ile kayıtlı bir kullanıcı bulunamadı");
        }

        return userRepository.findByEmail(email)
                .map(user -> {
                    log.info("Kullanıcı bulundu: {}", user.getEmail());

                    String resetToken = UUID.randomUUID().toString();
                    LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);

                    user.setPasswordResetToken(resetToken);
                    user.setPasswordResetTokenExpiry(tokenExpiry);
                    userRepository.save(user);

                    log.info("Token oluşturuldu: {}", resetToken);

                    String resetLink = FRONTEND_URL + "/auth" + RESET_PASSWORD_URL + resetToken;

                    try {
                        Map<String, Object> templateVariables = new HashMap<>();
                        templateVariables.put("title", "Şifre Sıfırlama İsteği");
                        templateVariables.put("userName", user.getFirstName() != null ? user.getFirstName() : "Değerli Kullanıcımız");
                        templateVariables.put("message", "Hesabınız için bir şifre sıfırlama talebi aldık. Şifrenizi sıfırlamak için aşağıdaki butona tıklayın.");
                        templateVariables.put("actionUrl", resetLink);
                        templateVariables.put("actionText", "Şifremi Sıfırla");
                        templateVariables.put("expiryTime", "24 saat");

                        String emailContent = emailTemplateService.processTemplate("password-reset", templateVariables);

                        NotificationDTO notificationDTO = NotificationDTO.builder()
                                .recipient(user.getEmail())
                                .subject("Şifre Sıfırlama İsteği")
                                .content(emailContent)
                                .type(NotificationType.INFO)
                                .isHtml(true)
                                .build();

                        log.info("Email gönderiliyor: {}", user.getEmail());
                        notificationService.sendEmail(notificationDTO);
                        log.info("Email başarıyla gönderildi");

                    } catch (NotificationException e) {
                        log.error("Email gönderme hatası: {}", e.getMessage(), e);
                        throw new NotificationException("Şifre sıfırlama e-postası gönderilirken bir hata oluştu: " + e.getMessage());
                    }
                    return Response.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi")
                            .build();
                })
                .orElseThrow(() -> new IllegalStateException("Bu hata asla oluşmamalı - Kontrol zaten yapıldı"));
    }

    @Override
    public Response<Void> resetPassword(ResetPasswordRequest request, String token) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Yeni şifre ve şifre tekrarı eşleşmiyor");
        }
        return userRepository.findByPasswordResetToken(token)
                .filter(user -> {
                    return user.getPasswordResetTokenExpiry() != null &&
                           user.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now());
                })
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(request.getPassword()));

                    user.setPasswordResetToken(null);
                    user.setPasswordResetTokenExpiry(null);
                    userRepository.save(user);

                    return Response.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Şifreniz başarıyla sıfırlandı")
                            .build();
                })
                .orElseThrow(() -> {
                    log.error("Geçersiz veya süresi dolmuş token: {}", token);
                    return new InvalidOrExpiredTokenException("Geçersiz veya süresi dolmuş token");
                });
    }
}
