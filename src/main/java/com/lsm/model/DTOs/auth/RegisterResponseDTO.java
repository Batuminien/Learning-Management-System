package com.lsm.model.DTOs.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lsm.model.entity.enums.Role;
import com.lsm.model.entity.enums.SchoolLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Response object for successful registration")
public class RegisterResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Unique identifier of the registered user")
    private Long userId;

    @Schema(description = "Username of the registered user")
    private String username;

    @Schema(description = "Name of the registered user")
    private String name;

    @Schema(description = "Surname of the registered user")
    private String surname;

    @Schema(description = "Email of the registered user")
    private String email;

    @Schema(description = "Role assigned to the user")
    private Role role;

    @Schema(description = "School level of the user")
    private SchoolLevel schoolLevel;

    @Schema(description = "Registration success message")
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Timestamp of registration")
    private Instant registeredAt;

    @Schema(description = "Verification status of the account")
    @Builder.Default
    private boolean verified = false;

    @Schema(description = "Next steps or instructions for the user")
    private List<String> nextSteps;
}
