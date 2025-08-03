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
        String email = loginRequest.getEmail();
        log.info("Giriş isteği alındı. email: {}", email);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.debug("Authentication başarılı. email: {}", userDetails.getUsername());

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> {
                        log.error("Kullanıcı bulunamadı. email: {}", userDetails.getUsername());
                        return new UserNotFoundException(userDetails.getUsername());
                    });

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            log.info("Son giriş tarihi güncellendi. email: {}", user.getEmail());

            String token = jwtUtil.generateToken(user.getEmail());
            log.debug("JWT token üretildi. email: {}", user.getEmail());

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setFirstLogin(user.isFirstLogin());
            log.info("Giriş başarılı. email: {}", user.getEmail());

            return Response.<LoginResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Giriş başarılı")
                    .data(loginResponse)
                    .build();

        } catch (InvalidCredentialsException e) {
            log.warn("Geçersiz giriş denemesi. email: {}", email);
            return Response.<LoginResponse>builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("E-posta veya şifre hatalı")
                    .build();
        } catch (Exception e) {
            log.error("Giriş işlemi sırasında beklenmeyen bir hata oluştu. email: {}, hata: {}", email, e.getMessage(), e);
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

        log.info("Şifre değiştirme isteği alındı. email: {}", authUser.getUsername());

        User user = userRepository.findByEmail(authUser.getUsername())
                .orElseThrow(() -> {
                    log.error("Şifre değiştirme sırasında kullanıcı bulunamadı. email: {}", authUser.getUsername());
                    return new UserNotFoundException(authUser.getUsername());
                });

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Mevcut şifre hatalı. email: {}", authUser.getUsername());
            throw new InvalidCredentialsException("Mevcut şifre hatalı");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Yeni şifre ve tekrarı uyuşmuyor. email: {}", authUser.getUsername());
            throw new InvalidCredentialsException("Yeni şifre ve şifre tekrarı eşleşmiyor");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        log.debug("Yeni şifre encode edildi. email: {}", authUser.getUsername());

        // İlk kez giriş yapmışsa, artık ilk giriş değil
        if (user.isFirstLogin()) {
            user.setFirstLogin(false);
            log.debug("Kullanıcının ilk giriş durumu güncellendi. email: {}", authUser.getUsername());
        }
        userRepository.save(user);
        log.info("Şifre başarıyla değiştirildi. email: {}", authUser.getUsername());

        return Response.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Şifreniz başarıyla değiştirildi")
                .build();
    }

    @Override
    public Response<Void> forgotPassword(String email) {
        log.info("Şifre sıfırlama talebi alındı. email: {}", email);

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

                    log.debug("Şifre sıfırlama token'ı oluşturuldu ve kaydedildi. email: {}, token: {}", user.getEmail(), resetToken);

                    String resetLink = FRONTEND_URL + "/auth" + RESET_PASSWORD_URL + resetToken;

                    try {
                        Map<String, Object> templateVariables = new HashMap<>();
                        templateVariables.put("title", "Şifre Sıfırlama İsteği");
                        templateVariables.put("userName", user.getFirstName() != null ? user.getFirstName() : "Değerli Kullanıcımız");
                        templateVariables.put("message", "Hesabınız için bir şifre sıfırlama talebi aldık. Şifrenizi sıfırlamak için aşağıdaki butona tıklayın.");
                        templateVariables.put("actionUrl", resetLink);
                        templateVariables.put("actionText", "Şifremi Sıfırla");
                        templateVariables.put("expiryTime", "24 saat");

                        log.debug("Şifre sıfırlama email içeriği hazırlanıyor. email: {}", user.getEmail());
                        String emailContent = emailTemplateService.processTemplate("password-reset", templateVariables);

                        NotificationDTO notificationDTO = NotificationDTO.builder()
                                .recipient(user.getEmail())
                                .subject("Şifre Sıfırlama İsteği")
                                .content(emailContent)
                                .type(NotificationType.INFO)
                                .isHtml(true)
                                .build();

                        log.info("Şifre sıfırlama email gönderiliyor. email: {}", user.getEmail());
                        notificationService.sendEmail(notificationDTO);
                        log.info("Şifre sıfırlama email başarıyla gönderildi. email: {}", user.getEmail());

                    } catch (NotificationException e) {
                        log.error("Email gönderimi sırasında hata oluştu. email: {}, hata: {}", user.getEmail(), e.getMessage(), e);
                        throw new NotificationException("Şifre sıfırlama e-postası gönderilirken bir hata oluştu: " + e.getMessage());
                    }

                    log.info("Şifre sıfırlama işlemi tamamlandı. email: {}", user.getEmail());
                    return Response.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi")
                            .build();
                })
                .orElseThrow(() -> {
                    log.error("Beklenmeyen hata: Kullanıcı bulunamamasına rağmen map bloğuna girildi. email: {}", email);
                    return new IllegalStateException("Bu hata asla oluşmamalı - Kontrol zaten yapıldı");
                });
    }

    @Override
    public Response<Void> resetPassword(ResetPasswordRequest request, String token) {
        log.info("resetPassword çağrıldı. token: {}", token);

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Şifre ve şifre tekrarı uyuşmuyor.");
            throw new InvalidCredentialsException("Yeni şifre ve şifre tekrarı eşleşmiyor");
        }
        return userRepository.findByPasswordResetToken(token)
                .filter(user -> {
                    boolean isValid = user.getPasswordResetTokenExpiry() != null &&
                            user.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now());
                    if (!isValid) {
                        log.warn("Token süresi dolmuş. token: {}, email: {}", token, user.getEmail());
                    }
                    return isValid;
                })
                .map(user -> {
                    log.debug("Token geçerli. Şifre güncelleniyor. email: {}", user.getEmail());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));

                    user.setPasswordResetToken(null);
                    user.setPasswordResetTokenExpiry(null);
                    userRepository.save(user);
                    log.info("Şifre başarıyla sıfırlandı. email: {}", user.getEmail());

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
