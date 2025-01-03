package com.lsm.model.DTOs.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lsm.model.entity.enums.Role;

import com.lsm.model.entity.enums.SchoolLevel;
import com.lsm.model.validation.constraint.PasswordConstraint;
import com.lsm.model.validation.groups.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration request containing user details")
public class RegisterRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be between 3 and 60 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    @Schema(description = "Username for registration",
            example = "john.doe")
    private String username;

    @NotBlank(message = "Password is required")
    @PasswordConstraint(groups = ValidationGroups.Registration.class)
    @Schema(description = "Password for the account",
            example = "SecurePass123!")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address",
            regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
    @Schema(description = "Email address",
            example = "john.doe@example.com")
    private String email;

    @NotNull(message = "Role is required")
    @Schema(description = "User role",
            example = "ROLE_STUDENT",
            allowableValues = {"ROLE_STUDENT", "ROLE_TEACHER"})
    private Role role;

    // @NotNull(message = "School level is required")
    @Schema(description = "School level",
            example = "HIGH_SCHOOL",
            allowableValues = {"MID_SCHOOL", "HIGH_SCHOOL"})
    private SchoolLevel schoolLevel;

    @Schema(description = "First name", example = "John")
    @Pattern(regexp = "^[a-zA-Z\\s-]{2,50}$",
            message = "First name must be between 2 and 50 characters and contain only letters, spaces, and hyphens")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    @Pattern(regexp = "^[a-zA-Z\\s-]{2,50}$",
            message = "Last name must be between 2 and 50 characters and contain only letters, spaces, and hyphens")
    private String lastName;

    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s",
                Optional.ofNullable(firstName).orElse(""),
                Optional.ofNullable(lastName).orElse("")).trim();
    }
}
