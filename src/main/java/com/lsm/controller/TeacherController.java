package com.lsm.controller;

import com.lsm.mapper.UserMapper;
import com.lsm.model.DTOs.TeacherResponseDTO;
import com.lsm.model.DTOs.TeacherUpdateRequestDTO;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.service.AppUserService;
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
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Teacher Management", description = "APIs for managing teachers")
public class TeacherController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final AppUserService appUserService;

    @Operation(summary = "Get all teachers")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse_<List<TeacherResponseDTO>>> getAllTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AppUser> teachers = userService.getAllTeachers(PageRequest.of(page, size));
        List<TeacherResponseDTO> response = teachers.getContent().stream()
                .map(userMapper::toTeacherResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse_<>(true, "Teachers retrieved successfully", response));
    }

    @Operation(summary = "Get the teacher's info")
    @GetMapping("/{teacherId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ApiResponse_<TeacherResponseDTO>> getTeacherInfo(
            Authentication authentication,
            @PathVariable Long teacherId) {
        try {
            AppUser teacher = (AppUser) authentication.getPrincipal();
            if ((teacher.getRole().equals(Role.ROLE_STUDENT) || teacher.getRole().equals(Role.ROLE_TEACHER))
                    && !teacher.getId().equals(teacherId))
                throw new AccessDeniedException("Logged in user and student id doesn't match.");
            teacher = appUserService.getCurrentUserWithDetails(teacherId);

            TeacherResponseDTO response = userMapper.toTeacherResponse(teacher);
            return ResponseEntity.ok(new ApiResponse_<>(true, "Students retrieved successfully", response));
        } catch (AccessDeniedException e) {
            log.error("Access denied: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        }
    }

    @Operation(summary = "Update teacher details")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse_<TeacherResponseDTO>> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherUpdateRequestDTO updateRequest) {
        AppUser updatedTeacher = userService.updateTeacher(id, updateRequest);
        return ResponseEntity.ok(new ApiResponse_<>(true, "Teacher updated successfully",
                userMapper.toTeacherResponse(updatedTeacher)));
    }

    @Operation(summary = "Delete teacher")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse_<Void>> deleteTeacher(@PathVariable Long id) {
        userService.deleteTeacher(id);
        return ResponseEntity.ok(new ApiResponse_<>(true, "Teacher deleted successfully", null));
    }
}
