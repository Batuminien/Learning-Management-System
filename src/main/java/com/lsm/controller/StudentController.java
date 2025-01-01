package com.lsm.controller;

import com.lsm.mapper.UserMapper;
import com.lsm.model.DTOs.StudentResponseDTO;
import com.lsm.model.DTOs.StudentUpdateRequestDTO;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Student Management", description = "APIs for managing students")
public class StudentController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Get all students")
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse_<List<StudentResponseDTO>>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AppUser> students = userService.getAllStudents(PageRequest.of(page, size));
        List<StudentResponseDTO> response = students.getContent().stream()
                .map(userMapper::toStudentResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse_<>(true, "Students retrieved successfully", response));
    }

    @Operation(summary = "Get the student's info")
    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ApiResponse_<StudentResponseDTO>> getStudentInfo(
            Authentication authentication,
            @PathVariable Long studentId) {
        try {
            AppUser student = (AppUser) authentication.getPrincipal();
            if ((student.getRole().equals(Role.ROLE_STUDENT) || student.getRole().equals(Role.ROLE_TEACHER))
                    && !student.getId().equals(studentId))
                throw new AccessDeniedException("Logged in user and student id doesn't match.");

            StudentResponseDTO response = userMapper.toStudentResponse(student);
            return ResponseEntity.ok(new ApiResponse_<>(true, "Students retrieved successfully", response));
        } catch (AccessDeniedException e) {
            log.error("Access denied: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        }
    }

    @Operation(summary = "Update student details")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse_<StudentResponseDTO>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequestDTO updateRequest) {
        AppUser updatedStudent = userService.updateStudent(id, updateRequest);
        return ResponseEntity.ok(new ApiResponse_<>(true, "Student updated successfully",
                userMapper.toStudentResponse(updatedStudent)));
    }

    @Operation(summary = "Delete student")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse_<Void>> deleteStudent(@PathVariable Long id) {
        userService.deleteStudent(id);
        return ResponseEntity.ok(new ApiResponse_<>(true, "Student deleted successfully", null));
    }
}
