package com.investra.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserResponse {
    private String firstName;
    private String lastName;
    private String nationalityNumber;
    private String employeeNumber;
    private String phoneNumber;
    private String email;
    private String role;
}
