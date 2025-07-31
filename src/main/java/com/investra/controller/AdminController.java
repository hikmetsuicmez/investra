package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.Response;
import com.investra.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.User.BASE)
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<CreateUserResponse>> createUser(@RequestBody CreateUserRequest request) {
        Response<CreateUserResponse> response = adminService.createUser(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
