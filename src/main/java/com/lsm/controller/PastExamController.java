package com.lsm.controller;

import com.lsm.model.DTOs.*;
import com.lsm.model.entity.base.AppUser;
import com.lsm.service.PastExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/past-exams")
@Tag(name = "Past Exam Management", description = "APIs for managing past exams (TYT, AYT, YDT)")
@SecurityRequirement(name = "bearerAuth")
public class PastExamController {

    private final PastExamService pastExamService;

    @Autowired
    public PastExamController(PastExamService pastExamService) {
        this.pastExamService = pastExamService;
    }

    @Operation(summary = "Create a new past exam")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping
    public ResponseEntity<ApiResponse_<PastExamResponseDTO>> createPastExam(
            @Valid @RequestBody PastExamRequestDTO requestDTO,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            PastExamResponseDTO createdExam = pastExamService.createPastExam(loggedInUser, requestDTO);

            return new ResponseEntity<>(new ApiResponse_<>(
                    true,
                    "Past exam created successfully",
                    createdExam
            ), HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error creating past exam: ", e);
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get past exam by ID")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse_<PastExamResponseDTO>> getPastExamById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            PastExamResponseDTO exam = pastExamService.getPastExamById(loggedInUser, id);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Past exam retrieved successfully",
                    exam
            ));
        } catch (AccessDeniedException e) {
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving past exam: ", e);
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all past exams")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping
    public ResponseEntity<ApiResponse_<List<PastExamResponseDTO>>> getAllPastExams() {
        try {
            List<PastExamResponseDTO> exams = pastExamService.getAllPastExams();

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Past exams retrieved successfully",
                    exams
            ));
        } catch (Exception e) {
            log.error("Error retrieving all past exams: ", e);
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get student's past exam results")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse_<List<StudentExamResultResponseDTO>>> getStudentResults(
            @PathVariable Long studentId,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            List<StudentExamResultResponseDTO> results = pastExamService.getStudentResults(loggedInUser, studentId);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Student results retrieved successfully",
                    results
            ));
        } catch (AccessDeniedException e) {
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving student results: ", e);
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Update past exam")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse_<PastExamResponseDTO>> updatePastExam(
            @PathVariable Long id,
            @Valid @RequestBody PastExamRequestDTO requestDTO,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            PastExamResponseDTO updatedExam = pastExamService.updatePastExam(loggedInUser, id, requestDTO);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Past exam updated successfully",
                    updatedExam
            ));
        } catch (AccessDeniedException e) {
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating past exam: ", e);
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete past exam")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse_<Void>> deletePastExam(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            pastExamService.deletePastExam(loggedInUser, id);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Past exam deleted successfully",
                    null
            ));
        } catch (AccessDeniedException e) {
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting past exam: ", e);
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }
}
