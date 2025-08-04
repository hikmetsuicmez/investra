package com.investra.dtos.request;

import com.investra.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private Role role;
}
