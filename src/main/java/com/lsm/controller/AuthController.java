package com.lsm.controller;

import com.lsm.exception.*;
import com.lsm.model.DTOs.*;
import com.lsm.model.entity.base.AppUser;
import com.lsm.security.RateLimiter;
import com.lsm.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {
    private final AuthService authService;
    private final RateLimiter rateLimiter;

    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user with their credentials and returns JWT tokens"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "429", description = "Too many requests"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse_<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest request) {

        String clientIp = extractClientIp(request);

        try {
            rateLimiter.checkRateLimit(clientIp);

            AuthenticationResult result;
            try {
                result = authService.authenticate(loginRequest, clientIp);
            } catch (Exception e) {
                throw new AuthenticationException(e.getMessage());
            }

            LoginResponseDTO response = LoginResponseDTO.builder()
                    .accessToken(result.getAccessToken())
                    .refreshToken(result.getRefreshToken())
                    .user(UserDTO.fromEntity(result.getUser()))
                    .build();

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Authentication successful",
                    response
            ));

        } catch (RateLimitExceededException e) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiResponse_<>(false, e.getMessage(), null));
        } catch (AuthenticationException | AccountLockedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse_<>(false, e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Register new user",
            description = "Registers a new user with the provided details"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid registration data"),
            @ApiResponse(responseCode = "409", description = "Username/email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse_<RegisterResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO registerRequest) {

        try {
            AppUser newUser = authService.registerUser(registerRequest);

            RegisterResponseDTO response = RegisterResponseDTO.builder()
                    .id(newUser.getId())
                    .username(newUser.getUsername())
                    .email(newUser.getEmail())
                    .message("Registration successful")
                    .build();

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse_<>(true, "Registration successful", response));

        } catch (DuplicateResourceException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ApiResponse_<>(false, e.getMessage(), null));
        } catch (InvalidPasswordException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse_<>(false, e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Refresh token",
            description = "Get a new access token using a valid refresh token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse_<TokenRefreshResponseDTO>> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken) {

        try {
            TokenRefreshResult result = authService.refreshToken(refreshToken);

            TokenRefreshResponseDTO response = TokenRefreshResponseDTO.builder()
                    .accessToken(result.getAccessToken())
                    .refreshToken(result.getRefreshToken())
                    .build();

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Token refreshed successfully",
                    response
            ));

        } catch (TokenExpiredException | InvalidTokenException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse_<>(false, e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Logout user",
            description = "Invalidates the user's tokens"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse_<Void>> logout(
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {

        try {
            authService.logout(
                    token.replace("Bearer ", ""),
                    refreshToken
            );

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Logged out successfully",
                    null
            ));

        } catch (LogoutException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse_<>(false, e.getMessage(), null));
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
