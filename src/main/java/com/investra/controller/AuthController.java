package com.investra.controller;

import com.investra.dtos.request.ChangePasswordRequest;
import com.investra.dtos.request.LoginRequest;
import com.investra.dtos.request.ResetPasswordRequest;
import com.investra.dtos.response.LoginResponse;
import com.investra.dtos.response.Response;
import com.investra.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Response<Void>> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response<Void>> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(authService.forgotPassword(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response<Void>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request,
            @RequestParam String token) {
        return ResponseEntity.ok(authService.resetPassword(request, token));
    }
}
