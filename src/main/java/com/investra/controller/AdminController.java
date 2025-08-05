package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.docs.AdminApiDocs;
import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.UpdateUserResponse;
import com.investra.dtos.response.UserDTO;
import com.investra.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.User.BASE)
@RequiredArgsConstructor
public class AdminController implements AdminApiDocs {

    private final AdminService adminService;

    @PostMapping(ApiEndpoints.User.CREATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<CreateUserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        Response<CreateUserResponse> response = adminService.createUser(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping(ApiEndpoints.User.UPDATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<UpdateUserResponse>> updateUser(@PathVariable String employeeNumber, @RequestBody UpdateUserRequest request) {
        Response<UpdateUserResponse> response = adminService.updateUser(employeeNumber, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping(ApiEndpoints.User.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> deleteUser(@Valid @PathVariable String employeeNumber) {
        Response<Void> response = adminService.deleteUser(employeeNumber);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping(ApiEndpoints.User.GET_ALL)
    public ResponseEntity<Response<List<UserDTO>>> retrieveAllUsers() {
        Response<List<UserDTO>> responses = adminService.retrieveAllUsers();
        return ResponseEntity.ok(responses);
    }

    @GetMapping(ApiEndpoints.User.GET_BY_ID)
    public ResponseEntity<Response<UserDTO>> retrieveUser(@PathVariable Long userId) {
        Response<UserDTO> response = adminService.retrieveUser(userId);
        return ResponseEntity.ok(response);
    }


}