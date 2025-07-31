package com.investra.service;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.Response;

public interface AdminService {

    Response<CreateUserResponse> createUser(CreateUserRequest createUserRequest);
}
