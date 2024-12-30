package com.lsm.service;

import com.lsm.model.DTOs.ProfilePhotoResponseDTO;
import com.lsm.model.entity.Assignment;
import com.lsm.model.entity.AssignmentDocument;
import com.lsm.model.entity.base.AppUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.file-storage.base-path}")
    private String baseStoragePath;

    private static final long MAX_PROFILE_PHOTO_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_PHOTO_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/jpg"
    );

    public AssignmentDocument handleDocumentUpload(MultipartFile file, Assignment assignment, AppUser uploader) throws IOException {
        // Generate unique file name to prevent collisions
        String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());

        // Create directory if it doesn't exist
        String relativePath = String.format("assignments/%d/%s", assignment.getId(), uniqueFileName);
        Path fullPath = Paths.get(baseStoragePath, relativePath);
        Files.createDirectories(fullPath.getParent());

        // Save file to storage
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, fullPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Create and return AssignmentDocument entity
        return AssignmentDocument.builder()
                .fileName(file.getOriginalFilename())
                .filePath(fullPath.toString())
                .uploadTime(LocalDateTime.now())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .assignment(assignment)
                .uploadedBy(uploader)
                .build();
    }

    @Transactional
    public ProfilePhotoResponseDTO handleProfilePhotoUpload(MultipartFile file, AppUser user) throws IOException {
        validateProfilePhoto(file);

        String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
        String relativePath = String.format("profile-photos/%d/%s", user.getId(), uniqueFileName);
        Path fullPath = Paths.get(baseStoragePath, relativePath);
        Files.createDirectories(fullPath.getParent());

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, fullPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Delete old profile photo if exists
        if (user.getProfilePhotoUrl() != null) {
            deleteProfilePhoto(user);
        }

        return ProfilePhotoResponseDTO.builder()
                .photoUrl(relativePath)
                .filename(uniqueFileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadTime(LocalDateTime.now())
                .build();
    }

    @Transactional
    public void deleteProfilePhoto(AppUser user) throws IOException {
        if (user.getProfilePhotoUrl() != null) {
            Path photoPath = Paths.get(baseStoragePath, user.getProfilePhotoUrl());
            Files.deleteIfExists(photoPath);
        }
    }

    private void validateProfilePhoto(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_PROFILE_PHOTO_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }
        if (!ALLOWED_PHOTO_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG and PNG are allowed");
        }
    }

    private void deleteOldProfilePhoto(AppUser user) {
        if (user.getProfilePhotoFilename() != null) {
            try {
                Path oldPhotoPath = Paths.get(baseStoragePath, user.getProfilePhotoUrl());
                Files.deleteIfExists(oldPhotoPath);
            } catch (IOException e) {
                log.error("Failed to delete old profile photo for user {}: {}", user.getId(), e.getMessage());
            }
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = FilenameUtils.getExtension(originalFileName);
        String baseName = FilenameUtils.getBaseName(originalFileName);
        return String.format("%s_%s.%s",
                baseName,
                UUID.randomUUID().toString().substring(0, 8),
                extension);
    }
}
