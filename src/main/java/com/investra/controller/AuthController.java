package com.investra.controller;

import com.investra.constants.ApiEndpoints;
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
@RequestMapping(ApiEndpoints.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(ApiEndpoints.Auth.LOGIN)
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping(ApiEndpoints.Auth.CHANGE_PASSWORD)
    public ResponseEntity<Response<Void>> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(request));
    }

    @PostMapping(ApiEndpoints.Auth.FORGOT_PASSWORD)
    public ResponseEntity<Response<Void>> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(authService.forgotPassword(email));
    }

    @PostMapping(ApiEndpoints.Auth.RESET_PASSWORD)
    public ResponseEntity<Response<Void>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request,
            @RequestParam String token) {
        return ResponseEntity.ok(authService.resetPassword(request, token));
    }
}
