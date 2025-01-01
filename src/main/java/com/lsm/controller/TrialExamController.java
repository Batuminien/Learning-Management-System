package com.lsm.controller;

import com.lsm.model.DTOs.TrialExamRequestDTO;
import com.lsm.model.DTOs.TrialExamResponseDTO;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.service.TrialExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestController
@RequestMapping("/api/v1/trial-exams")
@Tag(name = "Trial Exam Management", description = "APIs for managing trial exams (TYT, AYT, YDT, LGS)")
@SecurityRequirement(name = "bearerAuth")
public class TrialExamController {
    private final TrialExamService trialExamService;

    public TrialExamController(TrialExamService trialExamService) {
        this.trialExamService = trialExamService;
    }

    @Operation(summary = "Evaluate results of the trial exam")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping
    public ResponseEntity<ApiResponse_<TrialExamResponseDTO>> evaluateTrialExam(
            @Valid @RequestBody TrialExamRequestDTO requestDTO,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            if (loggedInUser.getRole().equals(Role.ROLE_STUDENT) || loggedInUser.getRole().equals(Role.ROLE_TEACHER))
                throw new AccessDeniedException("Logged in user is not allowed to evaluate trial exam");

            validateFile(requestDTO.getResults());
            validateFile(requestDTO.getAnswerKey());

            TrialExamResponseDTO createdExam = trialExamService.evaluateTrialExam(loggedInUser, requestDTO);

            return new ResponseEntity<>(new ApiResponse_<>(
                    true,
                    "Past exam created successfully",
                    createdExam
            ), HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating trial exam: ", e);
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File is null");
        }

        // Only validate if a file is actually provided
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > 10_000_000) { // 10MB limit
            throw new IllegalArgumentException("File size exceeds maximum limit: 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("text/plain")) {
            throw new IllegalArgumentException("Invalid file type: " + contentType + " (should be text/plain)");
        }
    }
}
