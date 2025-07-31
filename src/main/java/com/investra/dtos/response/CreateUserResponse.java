package com.investra.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserResponse {

    private String firstName;

    private String lastName;

    private String sicilNo;

    private String role;

    private String tckn;

    private String phoneNumber;

    private String email;

    private String password;
}
