package com.lsm.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.lsm.model.DTOs.AnnouncementDTO;
import com.lsm.model.entity.base.AppUser;
import com.lsm.repository.DeviceTokenRepository;
import com.lsm.service.AnnouncementService;
import com.lsm.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
@Validated
@Tag(name = "Announcement Management", description = "APIs for managing announcements")
@SecurityRequirement(name = "bearerAuth")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final NotificationService notificationService;
    private final DeviceTokenRepository deviceTokenRepository;

    @Operation(summary = "Create a new announcement", description = "Only teachers, admins, and coordinators can create announcements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Announcement created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping
    public ResponseEntity<ApiResponse_<AnnouncementDTO>> createAnnouncement(
            @Valid @RequestBody AnnouncementDTO announcementDTO,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            AnnouncementDTO createdAnnouncement = announcementService.createAnnouncement(loggedInUser, announcementDTO);
            ApiResponse_<AnnouncementDTO> response = new ApiResponse_<>(
                    true,
                    "Announcement created successfully",
                    createdAnnouncement
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            log.error("Access denied in createAnnouncement: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in createAnnouncement: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get announcements by class", description = "Retrieve all announcements for a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Announcements retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    @GetMapping("/class/{classId}")
    public ResponseEntity<ApiResponse_<List<AnnouncementDTO>>> getAnnouncementsByClass(
            @PathVariable Long classId,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            List<AnnouncementDTO> announcements = announcementService.getAnnouncementsByClassId(loggedInUser, classId);
            ApiResponse_<List<AnnouncementDTO>> response = new ApiResponse_<>(
                    true,
                    "Announcements retrieved successfully",
                    announcements
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving announcements: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving announcements: " + e.getMessage());
        }
    }

    @Operation(summary = "Update an announcement", description = "Only teachers, admins, and coordinators can update announcements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Announcement updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Announcement not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse_<AnnouncementDTO>> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementDTO announcementDTO,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            AnnouncementDTO updatedAnnouncement = announcementService.updateAnnouncement(loggedInUser, id, announcementDTO);
            ApiResponse_<AnnouncementDTO> response = new ApiResponse_<>(
                    true,
                    "Announcement updated successfully",
                    updatedAnnouncement
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied in updateAnnouncement: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in updateAnnouncement: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete an announcement", description = "Only teachers, admins, and coordinators can delete announcements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Announcement deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Announcement not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse_<Void>> deleteAnnouncement(@PathVariable Long assignmentId, Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            announcementService.deleteAnnouncement(loggedInUser, assignmentId);
            ApiResponse_<Void> response = new ApiResponse_<>(
                    true,
                    "Announcement deleted successfully",
                    null
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Create multiple announcements", description = "Only teachers, admins, and coordinators can create announcements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Announcements created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse_<List<AnnouncementDTO>>> createBulkAnnouncements(
            @Valid @RequestBody List<AnnouncementDTO> announcementDTOs,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            List<AnnouncementDTO> createdAnnouncements = new ArrayList<>();

            for (AnnouncementDTO dto : announcementDTOs) {
                AnnouncementDTO createdAnnouncement = announcementService.createAnnouncement(loggedInUser, dto);
                createdAnnouncements.add(createdAnnouncement);
            }

            ApiResponse_<List<AnnouncementDTO>> response = new ApiResponse_<>(
                    true,
                    "Announcements created successfully",
                    createdAnnouncements
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            log.error("Access denied in createBulkAnnouncements: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in createBulkAnnouncements: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Update multiple announcements", description = "Only teachers, admins, and coordinators can update announcements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Announcements updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "One or more announcements not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PutMapping("/bulk")
    public ResponseEntity<ApiResponse_<List<AnnouncementDTO>>> updateBulkAnnouncements(
            @Valid @RequestBody List<AnnouncementDTO> announcementDTOs,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            List<AnnouncementDTO> updatedAnnouncements = new ArrayList<>();

            for (AnnouncementDTO dto : announcementDTOs) {
                if (dto.getId() == null) {
                    throw new IllegalArgumentException("Announcement ID cannot be null for update operation");
                }
                AnnouncementDTO updatedAnnouncement = announcementService.updateAnnouncement(loggedInUser, dto.getId(), dto);
                updatedAnnouncements.add(updatedAnnouncement);
            }

            ApiResponse_<List<AnnouncementDTO>> response = new ApiResponse_<>(
                    true,
                    "Announcements updated successfully",
                    updatedAnnouncements
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied in updateBulkAnnouncements: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid request in updateBulkAnnouncements: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in updateBulkAnnouncements: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete multiple announcements", description = "Only teachers, admins, and coordinators can delete announcements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Announcements deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "One or more announcements not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @DeleteMapping("/bulk")
    public ResponseEntity<ApiResponse_<Void>> deleteBulkAnnouncements(
            @RequestBody List<Long> announcementIds,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();

            for (Long id : announcementIds) {
                announcementService.deleteAnnouncement(loggedInUser, id);
            }

            ApiResponse_<Void> response = new ApiResponse_<>(
                    true,
                    "Announcements deleted successfully",
                    null
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied in deleteBulkAnnouncements: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in deleteBulkAnnouncements: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Send push notification for an announcement",
            description = "Sends push notification to all students in all classes associated with the announcement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Announcement not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping("/{announcementId}/notify")
    public ResponseEntity<ApiResponse_<Void>> sendAnnouncementNotification(
            @PathVariable Long announcementId,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();

            sendNotification(loggedInUser, announcementId);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Notification sent successfully",
                    null
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied for sending announcement notification: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending notification: " + e.getMessage());
        }
    }

    @Operation(summary = "Send push notification for multiple announcements",
            description = "Sends push notifications for multiple announcements to students in all associated classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications sent successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping("/notify")
    public ResponseEntity<ApiResponse_<Void>> sendBulkAnnouncementNotifications(
            @RequestBody List<Long> announcementIds,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();

            for (Long announcementId : announcementIds) {
                sendNotification(loggedInUser, announcementId);
            }

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Notifications sent successfully",
                    null
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied in sending bulk announcement notification: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error sending notifications: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending notifications: " + e.getMessage());
        }
    }

    private void sendNotification(AppUser loggedInUser, Long announcementId)
            throws FirebaseMessagingException, AccessDeniedException {
        AnnouncementDTO announcement = announcementService.getAnnouncementById(loggedInUser, announcementId);

        // Collect unique device tokens from all classes
        Set<String> deviceTokens = new HashSet<>();
        for (Long classId : announcement.getClassIds()) {
            deviceTokens.addAll(deviceTokenRepository.findTokensByClassId(classId));
        }

        // Send notifications to all collected device tokens
        for (String token : deviceTokens) {
            notificationService.sendNotification(
                    token,
                    "New Announcement",
                    announcement.getTitle()
            );
        }
    }
}