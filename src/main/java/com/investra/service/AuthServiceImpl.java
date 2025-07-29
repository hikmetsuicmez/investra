package com.investra.service;

import com.investra.dtos.request.ChangePasswordRequest;
import com.investra.dtos.request.LoginRequest;
import com.investra.dtos.response.LoginResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.User;
import com.investra.repository.UserRepository;
import com.investra.security.AuthUser;
import com.investra.security.JwtUtil;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Doğrulama başarılıysa UserDetails nesnesini al
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

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
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return Response.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("Mevcut şifre hatalı")
                    .build();
        }

        // Yeni şifre ile onay eşleşiyor mu kontrol et
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Response.<Void>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("Yeni şifre ve onay şifresi eşleşmiyor")
                    .build();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // İlk giriş ise, artık ilk giriş değil
        if (user.isFirstLogin()) {
            user.setFirstLogin(false);
        }

        userRepository.save(user);

        return Response.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Şifreniz başarıyla değiştirildi")
                .build();
    }
}
