package com.lsm.model.DTOs.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.model.entity.enums.SchoolLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Response object containing user details and authentication tokens")
public class LoginResponseDTO {
    @Schema(description = "User's unique identifier")
    private Long id;

    @Schema(description = "Username of the authenticated user")
    private String username;

    @Schema(description = "Name of the authenticated user")
    private String name;

    @Schema(description = "Surname of the authenticated user")
    private String surname;

    @Schema(description = "Email address of the authenticated user")
    private String email;

    @Schema(description = "Role assigned to the user")
    private Role role;

    @Schema(description = "School level of the user")
    private SchoolLevel schoolLevel;

    @Schema(description = "JWT access token for authentication")
    private String accessToken;

    @Schema(description = "Refresh token for obtaining new access tokens")
    private String refreshToken;

    @Schema(description = "Token expiration time in milliseconds")
    private Long expiresIn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Timestamp when the token was issued")
    private Instant issuedAt;

    @Builder.Default
    @Schema(description = "Type of token (always 'Bearer')")
    private String tokenType = "Bearer";

    public LoginResponseDTO(AppUser appUser, String accessToken, String refreshToken, Long expiresIn) {
        this.id = appUser.getId();
        this.username = appUser.getUsername();
        this.name = appUser.getName();
        this.surname = appUser.getSurname();
        this.email = appUser.getEmail();
        this.role = appUser.getRole();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.issuedAt = Instant.now();
    }

    @JsonIgnore
    public String getFullToken() {
        return this.tokenType + " " + this.accessToken;
    }
}
