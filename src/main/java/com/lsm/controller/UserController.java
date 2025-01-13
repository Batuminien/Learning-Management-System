package com.lsm.controller;

import com.lsm.mapper.UserMapper;
import com.lsm.model.DTOs.UserResponseDTO;
import com.lsm.model.DTOs.UserUpdateRequestDTO;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Get all users")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse_<List<UserResponseDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AppUser> users = userService.getAllUsers(PageRequest.of(page, size));
        List<UserResponseDTO> response = users.getContent().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse_<>(true, "Users retrieved successfully", response));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse_<UserResponseDTO>> getUserById(@PathVariable Long id) {
        AppUser user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse_<>(true, "User retrieved successfully",
                userMapper.toUserResponse(user)));
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse_<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDTO updateRequest) {
        AppUser updatedUser = userService.updateUser(id, updateRequest);
        return ResponseEntity.ok(new ApiResponse_<>(true, "User updated successfully",
                userMapper.toUserResponse(updatedUser)));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse_<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "User deletion initiated. If this is an admin account, a verification email has been sent.",
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse_<>(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }

    @Operation(summary = "Confirm admin deletion")
    @PostMapping("/admin/confirm-deletion")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse_<Void>> confirmAdminDeletion(
            @RequestParam String token) {
        try {
            userService.confirmAdminDeletion(token);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Admin account deleted successfully",
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse_<>(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }
}
