package com.lsm.controller;

import com.lsm.model.DTOs.auth.*;
import com.lsm.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse_<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            passwordResetService.requestPasswordReset(request, httpRequest.getRemoteAddr());
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Password reset email has been sent if the email exists in our system",
                    null
            ));
        } catch (Exception e) {
            return ApiResponse_.httpError(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse_<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmDTO request) {
        passwordResetService.confirmPasswordReset(request);
        return ResponseEntity.ok(new ApiResponse_<>(
                true,
                "Password has been reset successfully",
                null
        ));
    }
}