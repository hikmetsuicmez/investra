package com.investra.mapper;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.UpdateUserResponse;
import com.investra.dtos.response.UserDTO;
import com.investra.entity.User;

import java.util.Optional;
import java.time.LocalDateTime;

public class UserMapper {

    public static User toEntity(CreateUserRequest request, String encodedPassword, String employeeNumber) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .nationalityNumber(request.getNationalityNumber())
                .employeeNumber(employeeNumber)
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .firstLogin(true)
                .role(request.getRole())
                .password(encodedPassword)
                .build();
    }

    public static CreateUserResponse toResponse(CreateUserRequest request, String rawPassword,String employeeNumber) {
        return CreateUserResponse.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .nationalityNumber(request.getNationalityNumber())
                .employeeNumber(employeeNumber)
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .role(request.getRole())
                .password(rawPassword)
                .createdDate(LocalDateTime.now())
                .build();
    }

    public static void updateFields(User user, UpdateUserRequest request) {
        Optional.ofNullable(request.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(request.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(request.getPhoneNumber()).ifPresent(user::setPhoneNumber);
        Optional.ofNullable(request.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(request.getRole()).ifPresent(user::setRole);
    }

    public static UpdateUserResponse toUpdateResponse(User user) {
        return UpdateUserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .nationalityNumber(user.getNationalityNumber())
                .employeeNumber(user.getEmployeeNumber())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public static UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .nationalityNumber(user.getNationalityNumber())
                .employeeNumber(user.getEmployeeNumber())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .build();
    }

}