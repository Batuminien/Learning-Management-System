package com.lsm.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.lsm.config.ClientType;
import com.lsm.config.ClientTypeInterceptor;
import com.lsm.mapper.AssignmentDocumentMapper;
import com.lsm.model.DTOs.*;
import com.lsm.model.entity.Assignment;
import com.lsm.model.entity.AssignmentDocument;
import com.lsm.model.entity.DeviceToken;
import com.lsm.model.entity.StudentSubmission;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.DeviceTokenRepository;
import com.lsm.service.AssignmentDocumentService;
import com.lsm.service.AssignmentService;

import com.lsm.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Assignment Management", description = "APIs for managing student assignments")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentDocumentService documentService;
    private final AssignmentDocumentMapper assignmentDocumentMapper;
    private final NotificationService notificationService;
    private final DeviceTokenRepository deviceTokenRepository;

    @Operation(
            summary = "Create a new assignment",
            description = "Allows teachers to create a new assignment for students"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assignment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping("/createAssignment")
    public ResponseEntity<ApiResponse_<AssignmentDTO>> createAssignment(
            @Valid @RequestBody AssignmentRequestDTO assignmentRequest,
            Authentication authentication
    ) throws AccessDeniedException {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Creating assignment for teacher: {}", currentUser.getUsername());

            if (currentUser.getRole() == Role.ROLE_STUDENT)
                throw new AccessDeniedException("Students cannot create assignments");

            Assignment assignment = assignmentService.createAssignment(assignmentRequest, currentUser.getId());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse_<>(
                            true,
                            "Assignment created successfully",
                            new AssignmentDTO(assignment, "Created successfully")
                    ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while creating assignment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, "Error creating assignment: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Update an assignment",
        description = "Allows teachers to update their own assignments"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assignment updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PutMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse_<AssignmentDTO>> updateAssignment(
            @Parameter(description = "ID of the assignment to update", required = true)
            @PathVariable @Positive Long assignmentId,
            @Valid @RequestBody AssignmentRequestDTO updateRequest,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            if (currentUser.getRole() == Role.ROLE_TEACHER && currentUser.equals(assignmentService.findById(assignmentId).getAssignedBy())) {
                throw new AccessDeniedException("Teachers can only update their own assignments");
            }
            Assignment updated = assignmentService.updateAssignment(assignmentId, updateRequest, currentUser.getId());
            
            return ResponseEntity.ok(new ApiResponse_<>(
                true,
                "Assignment updated successfully",
                new AssignmentDTO(updated, "Updated successfully")
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while updating assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred while updating assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get all assignments",
            description = "Retrieve all assignments in the system. Only accessible by administrators and coordinators."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping
    public ResponseEntity<ApiResponse_<List<AssignmentDTO>>> getAllAssignments(
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            List<AssignmentDTO> assignments = assignmentService.getAllAssignments(currentUser);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "All assignments retrieved successfully",
                    assignments
            ));

        } catch (AccessDeniedException e) {
            log.error("Access denied while getting all assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get teacher's assignments",
            description = "Retrieve all assignments created by a specific teacher"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse_<List<AssignmentDTO>>> getTeacherAssignments(
            @Parameter(description = "ID of the teacher", required = true)
            @PathVariable @Positive Long teacherId,
            @Parameter(description = "Class ID filter")
            @RequestParam(required = false) Long classId,
            @Parameter(description = "Course ID filter")
            @RequestParam(required = false) Long courseId,
            @Parameter(description = "Due date filter")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            // Additional security check to ensure teachers can only view their own assignments
            if (currentUser.getRole() == Role.ROLE_TEACHER && !currentUser.getId().equals(teacherId)) {
                throw new AccessDeniedException("Teachers can only view their own assignments");
            }

            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByTeacher
                    (teacherId, classId, courseId, dueDate, currentUser);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Teacher assignments retrieved successfully",
                    assignments
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while getting teacher assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            log.error("Entity not found while getting teacher assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, "Entity not found: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Delete assignment document",
            description = "Delete a document associated with an assignment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<ApiResponse_<Void>> deleteDocument(
            @Parameter(description = "ID of the document to delete", required = true)
            @PathVariable @Positive Long documentId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Deleting document: {}, user: {}", documentId, currentUser.getUsername());

            // Check if the user is a teacher and owns the assignment
            if (currentUser.getRole() == Role.ROLE_TEACHER) {
                AssignmentDocument document = documentService.findById(documentId);
                if (!currentUser.getId().equals(document.getAssignment().getAssignedBy().getId())) {
                    throw new AccessDeniedException("Teachers can only delete documents from their own assignments");
                }
            }

            documentService.deleteDocument(documentId, currentUser);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Document deleted successfully",
                    null
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while deleting document: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            log.error("Document not found: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, "Entity not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting document: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, "An error occurred: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get assignments by student",
            description = "Retrieve all assignments assigned to a specific student"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse_<List<StudentAssignmentViewDTO>>> getAssignmentsByStudent(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Positive Long studentId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            // Students can only view their own assignments
            if (currentUser.getRole() == Role.ROLE_STUDENT && !currentUser.getId().equals(studentId)) {
                throw new AccessDeniedException("Students can only view their own assignments");
            }

            List<StudentAssignmentViewDTO> assignments = assignmentService.getAssignmentsByStudent(studentId);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Student assignments retrieved successfully",
                    assignments
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while getting assignments of the student: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            log.error("Student not found: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, "Student not found: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Delete an assignment",
        description = "Allows teachers to delete their own assignments"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assignment deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse_<Void>> deleteAssignment(
            @Parameter(description = "ID of the assignment to delete", required = true)
            @PathVariable @Positive Long assignmentId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            assignmentService.deleteAssignment(assignmentId, currentUser);
            
            return ResponseEntity.ok(new ApiResponse_<>(
                true,
                "Assignment deleted successfully",
                null
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while deleting an assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        }
    }

    @Operation(summary = "Upload assignment document")
    @PostMapping("/{assignmentId}/documents")
    public ResponseEntity<ApiResponse_<AssignmentDocumentDTO>> uploadDocument(
            @PathVariable @Positive Long assignmentId,
            @RequestParam("file") @NotNull MultipartFile file,
            Authentication authentication) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Uploading document for assignment: {}, user: {}", assignmentId, currentUser.getUsername());

            // Validate file size and type
            validateFile(file);

            AssignmentDocument document = documentService.uploadDocument(
                    file, assignmentId, currentUser);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Document uploaded successfully",
                    assignmentDocumentMapper.convertToDTO(document)
            ));
        } catch (IOException e) {
            log.error("Error uploading document: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, "Error uploading document: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Download assignment document",
            description = "Download a document associated with an assignment"
    )
    @GetMapping("/documents/{documentId}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId,
            Authentication authentication) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            Resource resource = documentService.downloadDocument(documentId, currentUser);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("Error downloading document: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Grade an assignment",
            description = "Allows teachers to grade submitted assignments and provide feedback"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment graded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid grade data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PatchMapping("/{assignmentId}/{studentId}/grade")
    public ResponseEntity<ApiResponse_<AssignmentDTO>> gradeAssignment(
            @PathVariable @Positive Long assignmentId,
            @PathVariable @Positive Long studentId,
            @Valid @RequestBody GradeDTO gradeDTO,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            Assignment graded = assignmentService.gradeAssignment(assignmentId, gradeDTO, currentUser, studentId);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Assignment graded successfully",
                    new AssignmentDTO(graded, "Graded successfully")
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while grading the assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Bulk grade assignments",
            description = "Allows teachers to grade multiple student submissions for an assignment at once"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments graded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid grade data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PatchMapping("/{assignmentId}/bulk-grade")
    public ResponseEntity<ApiResponse_<List<AssignmentDTO>>> bulkGradeAssignment(
            @Parameter(description = "ID of the assignment to grade", required = true)
            @PathVariable @Positive Long assignmentId,
            @Valid @RequestBody BulkGradeRequestDTO bulkGradeRequest,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Bulk grading assignment: {}, user: {}", assignmentId, currentUser.getUsername());

            List<Assignment> gradedAssignments = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (BulkGradeRequestDTO.BulkGradeItem gradeItem : bulkGradeRequest.getGrades()) {
                try {
                    Assignment graded = assignmentService.gradeAssignment(
                            assignmentId,
                            gradeItem.getGrade(),
                            currentUser,
                            gradeItem.getStudentId()
                    );
                    gradedAssignments.add(graded);
                } catch (Exception e) {
                    errors.add("Failed to grade student " + gradeItem.getStudentId() + ": " + e.getMessage());
                    log.error("Error grading assignment for student {}: {}", gradeItem.getStudentId(), e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.PARTIAL_CONTENT)
                        .body(new ApiResponse_<>(
                                false,
                                "Some grades failed to be applied: " + String.join("; ", errors),
                                gradedAssignments.stream()
                                        .map(a -> new AssignmentDTO(a, "Graded successfully"))
                                        .collect(Collectors.toList())
                        ));
            }

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "All assignments graded successfully",
                    gradedAssignments.stream()
                            .map(a -> new AssignmentDTO(a, "Graded successfully"))
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            log.error("Error during bulk grading: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, "Error during bulk grading: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Submit an assignment",
            description = "Allows students to submit their assignments with documents before the deadline"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid submission or past deadline"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PatchMapping(value = "/{assignmentId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse_<StudentSubmissionDTO>> submitAssignment(
            @Parameter(description = "ID of the assignment to submit", required = true)
            @PathVariable @Positive Long assignmentId,
            @Valid @ModelAttribute SubmitAssignmentDTO submitDTO,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Submitting assignment: {}, user: {}", assignmentId, currentUser.getUsername());

            // Validate file
            validateFile(submitDTO.getDocument());

            StudentSubmission submission = assignmentService.submitAssignment(assignmentId, submitDTO, currentUser);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Assignment submitted successfully",
                    new StudentSubmissionDTO(submission)
            ));
        } catch (IOException e) {
            log.error("An exception occurred when submitting an assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "IOException occurred: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error submitting assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Error submitting assignment: " + e.getMessage());
        }
    }


    @Operation(
            summary = "Un-submit an assignment",
            description = "Allows students to un-submit their assignments if they haven't been graded yet"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment unsubmitted successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PatchMapping("/{assignmentId}/unsubmit")
    public ResponseEntity<ApiResponse_<AssignmentDTO>> unsubmitAssignment(
            @PathVariable @Positive Long assignmentId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            Assignment unsubmitted = assignmentService.unsubmitAssignment(assignmentId, currentUser);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Assignment unsubmitted successfully",
                    new AssignmentDTO(unsubmitted, "Unsubmitted successfully")
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while un-submitting the assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error while un-submitting the assignment: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Error: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Send new assignment notification",
            description = "Sends push notifications about a new assignment to all students in the class (Mobile only)"
    )
    @PostMapping("/{assignmentId}/notify/created")
    public ResponseEntity<ApiResponse_<Void>> notifyAssignmentCreated(
            @PathVariable @Positive Long assignmentId,
            Authentication authentication
    ) {
        try {
            // Verify this is a mobile client
            if (ClientTypeInterceptor.getCurrentClientType() == ClientType.WEB) {
                return ApiResponse_.httpError(
                        HttpStatus.BAD_REQUEST,
                        "This endpoint is only available for mobile clients"
                );
            }

            AppUser currentUser = (AppUser) authentication.getPrincipal();
            Assignment assignment = assignmentService.findById(assignmentId);

            // Verify the user is the assignment creator
            if (!assignment.getAssignedBy().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Only the assignment creator can send notifications");
            }

            List<String> deviceTokens = deviceTokenRepository.findTokensByClassId(assignment.getClassEntity().getId());
            for (String token : deviceTokens) {
                try {
                    notificationService.sendNotification(
                            token,
                            "New Assignment",
                            "A new assignment has been posted: " + assignment.getTitle()
                    );
                } catch (FirebaseMessagingException e) {
                    log.error("Failed to send notification to token {}: {}", token, e.getMessage());
                }
            }

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Notifications sent successfully",
                    null
            ));
        } catch (Exception e) {
            log.error("Error sending notifications: {}", e.getMessage());
            return ApiResponse_.httpError(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send notifications: " + e.getMessage()
            );
        }
    }

    @Operation(
            summary = "Send grade notification",
            description = "Sends push notification about graded assignment to the student (Mobile only)"
    )
    @PostMapping("/{assignmentId}/students/{studentId}/notify/graded")
    public ResponseEntity<ApiResponse_<Void>> notifyAssignmentGraded(
            @PathVariable @Positive Long assignmentId,
            @PathVariable @Positive Long studentId,
            Authentication authentication
    ) {
        try {
            if (ClientTypeInterceptor.getCurrentClientType() == ClientType.WEB) {
                return ApiResponse_.httpError(
                        HttpStatus.BAD_REQUEST,
                        "This endpoint is only available for mobile clients"
                );
            }

            AppUser currentUser = (AppUser) authentication.getPrincipal();
            Assignment assignment = assignmentService.findById(assignmentId);

            // Verify the user is the assignment creator
            if (!assignment.getAssignedBy().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Only the assignment creator can send notifications");
            }

            List<DeviceToken> tokens = deviceTokenRepository.findByUserId(studentId);
            for (DeviceToken token : tokens) {
                try {
                    notificationService.sendNotification(
                            token.getDeviceToken(),
                            "Assignment Graded",
                            "Your assignment '" + assignment.getTitle() + "' has been graded"
                    );
                } catch (FirebaseMessagingException e) {
                    log.error("Failed to send notification to student {}: {}", studentId, e.getMessage());
                }
            }

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Grade notification sent successfully",
                    null
            ));
        } catch (Exception e) {
            log.error("Error sending grade notification: {}", e.getMessage());
            return ApiResponse_.httpError(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send grade notification: " + e.getMessage()
            );
        }
    }

    @Operation(
            summary = "Send submission notification",
            description = "Sends push notification about assignment submission to the teacher (Mobile only)"
    )
    @PostMapping("/{assignmentId}/notify/submitted")
    public ResponseEntity<ApiResponse_<Void>> notifyAssignmentSubmitted(
            @PathVariable @Positive Long assignmentId,
            Authentication authentication
    ) {
        try {
            if (ClientTypeInterceptor.getCurrentClientType() == ClientType.WEB) {
                return ApiResponse_.httpError(
                        HttpStatus.BAD_REQUEST,
                        "This endpoint is only available for mobile clients"
                );
            }

            AppUser currentUser = (AppUser) authentication.getPrincipal();
            Assignment assignment = assignmentService.findById(assignmentId);

            List<DeviceToken> tokens = deviceTokenRepository.findByUserId(assignment.getAssignedBy().getId());
            for (DeviceToken token : tokens) {
                try {
                    notificationService.sendNotification(
                            token.getDeviceToken(),
                            "Assignment Submitted",
                            "Student " + currentUser.getUsername() + " has submitted assignment: " + assignment.getTitle()
                    );
                } catch (FirebaseMessagingException e) {
                    log.error("Failed to send notification to teacher: {}", e.getMessage());
                }
            }

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Submission notification sent successfully",
                    null
            ));
        } catch (Exception e) {
            log.error("Error sending submission notification: {}", e.getMessage());
            return ApiResponse_.httpError(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send submission notification: " + e.getMessage()
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            return;
        if (file.getSize() > 5_000_000) { // 5MB limit
            throw new IllegalArgumentException("File size exceeds maximum limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("Invalid file type");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        return Arrays.asList(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain"
        ).contains(contentType);
    }
}