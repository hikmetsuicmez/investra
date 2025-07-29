package com.investra.controller;

import com.investra.dtos.request.ChangePasswordRequest;
import com.investra.dtos.request.LoginRequest;
import com.investra.dtos.response.LoginResponse;
import com.investra.dtos.response.Response;
import com.investra.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
