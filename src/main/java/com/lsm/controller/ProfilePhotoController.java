package com.lsm.controller;

import com.lsm.model.DTOs.ProfilePhotoDTO;
import com.lsm.model.DTOs.ProfilePhotoResponseDTO;
import com.lsm.model.DTOs.ProfilePhotoUpdateRequestDTO;
import com.lsm.service.FileStorageService;
import com.lsm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.lsm.model.entity.base.AppUser;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/profile-photo")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile Photo", description = "Profile photo management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ProfilePhotoController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile photo", description = "Upload or update user's profile photo")
    public ResponseEntity<ApiResponse_<ProfilePhotoResponseDTO>> uploadProfilePhoto(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            ProfilePhotoResponseDTO photoDTO = fileStorageService.handleProfilePhotoUpload(file, currentUser);

            // Update user profile photo info
            userService.updateUserProfilePhoto(currentUser.getId(),
                    ProfilePhotoUpdateRequestDTO.builder()
                            .photoUrl(photoDTO.getPhotoUrl())
                            .filename(photoDTO.getFilename())
                            .build());

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Profile photo uploaded successfully",
                    photoDTO
            ));
        } catch (IOException e) {
            log.error("Failed to upload profile photo for user {}: {}", currentUser.getId(), e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload profile photo");
        } catch (IllegalArgumentException e) {
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping
    @Operation(summary = "Delete profile photo", description = "Remove user's profile photo")
    public ResponseEntity<ApiResponse_<Void>> deleteProfilePhoto(@AuthenticationPrincipal AppUser currentUser) {
        try {
            fileStorageService.deleteProfilePhoto(currentUser);
            userService.removeUserProfilePhoto(currentUser.getId());
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Profile photo deleted successfully",
                    null
            ));
        } catch (IOException e) {
            log.error("Failed to delete profile photo for user {}: {}", currentUser.getId(), e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete profile photo");
        }
    }

    @GetMapping
    @Operation(summary = "Get profile photo", description = "Get current user's profile photo information")
    public ResponseEntity<ApiResponse_<ProfilePhotoDTO>> getProfilePhoto(@AuthenticationPrincipal AppUser currentUser) {
        if (currentUser.getProfilePhotoUrl() == null) {
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "No profile photo found");
        }

        ProfilePhotoDTO photoDTO = ProfilePhotoDTO.builder()
                .photoUrl(currentUser.getProfilePhotoUrl())
                .filename(currentUser.getProfilePhotoFilename())
                .build();

        return ResponseEntity.ok(new ApiResponse_<>(
                true,
                "Profile photo retrieved successfully",
                photoDTO
        ));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user's profile photo", description = "Get profile photo information for any user")
    public ResponseEntity<ApiResponse_<ProfilePhotoDTO>> getUserProfilePhoto(@PathVariable Long userId) {
        try {
            AppUser user = userService.getUserById(userId);

            if (user.getProfilePhotoUrl() == null) {
                return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "No profile photo found for user");
            }

            ProfilePhotoDTO photoDTO = ProfilePhotoDTO.builder()
                    .photoUrl(user.getProfilePhotoUrl())
                    .filename(user.getProfilePhotoFilename())
                    .build();

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Profile photo retrieved successfully",
                    photoDTO
            ));
        } catch (Exception e) {
            log.error("Error retrieving profile photo for user {}: {}", userId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving profile photo");
        }
    }
}