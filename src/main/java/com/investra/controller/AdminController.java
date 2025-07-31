package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.UpdateUserResponse;
import com.investra.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/update-user/{employeeNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<UpdateUserResponse>> updateUser(
            @PathVariable String employeeNumber,
            @RequestBody UpdateUserRequest request) {
        Response<UpdateUserResponse> response = adminService.updateUser(employeeNumber, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
