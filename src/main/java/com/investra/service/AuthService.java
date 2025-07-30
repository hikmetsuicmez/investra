package com.investra.service;

import com.investra.dtos.request.ChangePasswordRequest;
import com.investra.dtos.request.LoginRequest;
import com.investra.dtos.request.ResetPasswordRequest;
import com.investra.dtos.response.LoginResponse;
import com.investra.dtos.response.Response;

public interface AuthService {

    Response<LoginResponse> login(LoginRequest loginRequest);

    Response<Void> changePassword(ChangePasswordRequest request);

    Response<Void> forgotPassword(String email);

    Response<Void> resetPassword(ResetPasswordRequest request, String token);
}
