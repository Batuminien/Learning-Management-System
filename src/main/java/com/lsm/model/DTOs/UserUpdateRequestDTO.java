package com.lsm.model.DTOs;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequestDTO {
    @Email
    private String email;
    private String firstName;
    private String lastName;
    private String password;
}
