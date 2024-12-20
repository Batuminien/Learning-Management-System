package com.lsm.controller;

import com.lsm.mapper.UserMapper;
import com.lsm.model.DTOs.TeacherResponseDTO;
import com.lsm.model.DTOs.TeacherUpdateRequestDTO;
import com.lsm.model.entity.base.AppUser;
import com.lsm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Teacher Management", description = "APIs for managing teachers")
public class TeacherController {
    private final UserService userService;
    private final UserMapper userMapper;

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
