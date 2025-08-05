package com.investra.service;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.UpdateUserResponse;
import com.investra.dtos.response.UserDTO;

import java.util.List;

public interface AdminService {

    Response<CreateUserResponse> createUser(CreateUserRequest createUserRequest);
    Response<UpdateUserResponse> updateUser(String employeeNumber ,UpdateUserRequest updateUserRequest);
    Response<Void> deleteUser(String employeeNumber);
    Response<List<UserDTO>> retrieveAllUsers();
    Response<UserDTO> retrieveUser(Long userId);

}
