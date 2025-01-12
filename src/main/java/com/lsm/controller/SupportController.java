package com.lsm.controller;

import com.lsm.model.DTOs.SupportRequestDTO;
import com.lsm.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {
    private final EmailService emailService;

    @PostMapping("/contact")
    public ResponseEntity<ApiResponse_<Void>> contactSupport(
            @Valid @RequestBody SupportRequestDTO request) {
        // TODO: Handle support request
        return ResponseEntity.ok(new ApiResponse_<>(
                true,
                "Support request received. We'll get back to you shortly.",
                null
        ));
    }
}
