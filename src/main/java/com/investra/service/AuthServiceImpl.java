package com.investra.service;

import com.investra.dtos.request.ChangePasswordRequest;
import com.investra.dtos.request.LoginRequest;
import com.investra.dtos.request.ResetPasswordRequest;
import com.investra.dtos.response.LoginResponse;
import com.investra.dtos.response.NotificationDTO;
import com.investra.dtos.response.Response;
import com.investra.entity.User;
import com.investra.enums.NotificationType;
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

        } catch (BadCredentialsException e) {
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
            return Response.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("Mevcut şifre hatalı")
                    .build();
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Response.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("Yeni şifre ve onay şifresi eşleşmiyor")
                    .build();
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

        return userRepository.findByEmail(email)
                .map(user -> {
                    System.out.println("Kullanıcı bulundu: " + user.getEmail());

                    String resetToken = UUID.randomUUID().toString();
                    LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);

                    user.setPasswordResetToken(resetToken);
                    user.setPasswordResetTokenExpiry(tokenExpiry);
                    userRepository.save(user);

                    System.out.println("Token oluşturuldu: " + resetToken);

                    // Şifre sıfırlama maili gönder - ortam değişkeni kullan
                    String resetLink =  FRONTEND_URL + "/auth" + RESET_PASSWORD_URL + resetToken;
                    String emailContent = "<h2>Şifre Sıfırlama İsteği</h2>"
                            + "<p>Şifrenizi sıfırlamak için aşağıdaki linke tıklayın:</p>"
                            + "<a href='" + resetLink + "'>Şifremi Sıfırla</a>"
                            + "<p>Bu link 24 saat içinde geçerliliğini yitirecektir.</p>";

                    NotificationDTO notificationDTO = NotificationDTO.builder()
                            .recipient(user.getEmail())
                            .subject("Şifre Sıfırlama İsteği")
                            .content(emailContent)
                            .type(NotificationType.INFO)
                            .isHtml(true)
                            .build();

                    System.out.println("Email gönderiliyor: " + resetLink);

                    try {
                        notificationService.sendEmail(notificationDTO);
                        System.out.println("Email başarıyla gönderildi");
                    } catch (Exception e) {
                        System.err.println("Email gönderme hatası: " + e.getMessage());
                        e.printStackTrace();
                    }

                    return Response.<Void>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi")
                            .data(null)
                            .build();
                })
                .orElse(Response.<Void>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi")
                        .data(null)
                        .build());
    }

    @Override
    public Response<Void> resetPassword(ResetPasswordRequest request, String token) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Response.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("Şifre ve şifre tekrarı eşleşmiyor")
                    .build();
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
                .orElse(Response.<Void>builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message("Geçersiz veya süresi dolmuş şifre sıfırlama bağlantısı")
                        .build());
    }
}
