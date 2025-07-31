package com.investra.mapper;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.entity.User;

public class UserMapper {

    public static User toEntity(CreateUserRequest request, String encodedPassword) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .tckn(request.getTckn())
                .sicilNo(request.getSicilNo())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .role(request.getRole())
                .password(encodedPassword)
                .build();
    }

    public static CreateUserResponse toResponse(CreateUserRequest request, String rawPassword) {
        return CreateUserResponse.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .tckn(request.getTckn())
                .sicilNo(request.getSicilNo())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .role(request.getRole().name())
                .password(rawPassword)
                .build();
    }
}