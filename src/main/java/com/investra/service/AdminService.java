package com.investra.service;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.UpdateUserResponse;

public interface AdminService {

    Response<CreateUserResponse> createUser(CreateUserRequest createUserRequest);
    Response<UpdateUserResponse> updateUser(String sicilNo ,UpdateUserRequest updateUserRequest);
}
