package com.investra.dtos.response;

import com.investra.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateUserResponse {

    private String firstName;

    private String lastName;

    private String sicilNo;

    private Role role;

    private String tckn;

    private String phoneNumber;

    private String email;

    private String password;

    private LocalDateTime createdDate;
}
