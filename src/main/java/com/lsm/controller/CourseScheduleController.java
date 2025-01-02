package com.lsm.controller;

import com.lsm.model.DTOs.CourseScheduleDTO;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.service.CourseScheduleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@Tag(name = "Course Schedule Management", description = "APIs for managing course schedules")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CourseScheduleController {

    private final CourseScheduleService courseScheduleService;

    @PostMapping
    @Operation(summary = "Create a course schedule", description = "Create a new schedule for a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Schedule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse_<CourseScheduleDTO>> createSchedule(
            @Valid @RequestBody CourseScheduleDTO scheduleDTO,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Creating new schedule for teacher course: {}", scheduleDTO.getTeacherCourseId());

            // Teachers can only create schedules for their own courses
            if (currentUser.getRole() == Role.ROLE_TEACHER
                    && !courseScheduleService.isTeachersCourse(currentUser.getId(), scheduleDTO.getTeacherCourseId())) {
                throw new AccessDeniedException("Teachers can only create schedules for their own courses");
            }

            CourseScheduleDTO createdSchedule = courseScheduleService.createSchedule(scheduleDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse_<>(
                            true,
                            "Schedule created successfully",
                            createdSchedule
                    ));
        } catch (EntityNotFoundException e) {
            log.error("Entity not found while creating schedule: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Entity not found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access denied while creating schedule: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error creating schedule: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating schedule: " + e.getMessage());
        }
    }

    @GetMapping("/teacher-course/{teacherCourseId}")
    @Operation(summary = "Get schedules by teacher course", description = "Retrieve all schedules for a specific teacher course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Teacher course not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ApiResponse_<List<CourseScheduleDTO>>> getSchedulesByTeacherCourse(
            @Parameter(description = "ID of the teacher course", required = true)
            @PathVariable @Positive Long teacherCourseId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Retrieving schedules for teacher course ID: {}", teacherCourseId);

            if (currentUser.getRole() == Role.ROLE_TEACHER
                    && !courseScheduleService.isTeachersCourse(currentUser.getId(), teacherCourseId)) {
                throw new AccessDeniedException("Teachers can only view schedules for their own courses");
            }

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Schedules retrieved successfully",
                    courseScheduleService.getSchedulesByTeacherCourse(teacherCourseId)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Teacher course not found with ID: {}", teacherCourseId);
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Teacher course not found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access denied for schedules: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving schedules for teacher course {}: {}", teacherCourseId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving schedules: " + e.getMessage());
        }
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get schedules by class", description = "Retrieve all schedules for a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ApiResponse_<List<CourseScheduleDTO>>> getSchedulesByClass(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable @Positive Long classId
    ) {
        try {
            log.info("Retrieving schedules for class ID: {}", classId);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Schedules retrieved successfully",
                    courseScheduleService.getSchedulesByClass(classId)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Class not found with ID: {}", classId);
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Class not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving schedules for class {}: {}", classId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving schedules: " + e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get schedules by student", description = "Retrieve all schedules for a specific student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ApiResponse_<List<CourseScheduleDTO>>> getSchedulesByStudent(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Positive Long studentId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Retrieving schedules for student ID: {}", studentId);

            // Students can only view their own schedules
            if (currentUser.getRole() == Role.ROLE_STUDENT && !currentUser.getId().equals(studentId)) {
                throw new AccessDeniedException("Students can only view their own schedules");
            }

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Student schedules retrieved successfully",
                    courseScheduleService.getSchedulesByStudent(studentId)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Student not found with ID: {}", studentId);
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Student not found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access denied for student schedules: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving schedules for student {}: {}", studentId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving schedules: " + e.getMessage());
        }
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get schedules by teacher", description = "Retrieve all schedules for a specific teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse_<List<CourseScheduleDTO>>> getSchedulesByTeacher(
            @Parameter(description = "ID of the teacher", required = true)
            @PathVariable @Positive Long teacherId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Retrieving schedules for teacher ID: {}", teacherId);

            // Teachers can only view their own schedules
            if (currentUser.getRole() == Role.ROLE_TEACHER && !currentUser.getId().equals(teacherId)) {
                throw new AccessDeniedException("Teachers can only view their own schedules");
            }

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Teacher schedules retrieved successfully",
                    courseScheduleService.getSchedulesByTeacher(teacherId)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Teacher not found with ID: {}", teacherId);
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Teacher not found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access denied for teacher schedules: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving schedules for teacher {}: {}", teacherId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving schedules: " + e.getMessage());
        }
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "Delete a schedule", description = "Delete an existing schedule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedule deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Schedule not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse_<Void>> deleteSchedule(
            @Parameter(description = "ID of the schedule to delete", required = true)
            @PathVariable @Positive Long scheduleId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Deleting schedule with ID: {}", scheduleId);

            // Teachers can only delete their own schedules
            if (currentUser.getRole() == Role.ROLE_TEACHER
                    && !courseScheduleService.isTeachersSchedule(currentUser.getId(), scheduleId)) {
                throw new AccessDeniedException("Teachers can only delete their own schedules");
            }

            courseScheduleService.deleteSchedule(scheduleId);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Schedule deleted successfully",
                    null
            ));
        } catch (EntityNotFoundException e) {
            log.error("Schedule not found with ID: {}", scheduleId);
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Schedule not found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access denied while deleting schedule: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting schedule {}: {}", scheduleId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting schedule: " + e.getMessage());
        }
    }

    @PutMapping("/{scheduleId}")
    @Operation(summary = "Update a course schedule", description = "Update an existing schedule for a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedule updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Schedule not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse_<CourseScheduleDTO>> updateSchedule(
            @Parameter(description = "ID of the schedule to update", required = true)
            @PathVariable @Positive Long scheduleId,
            @Valid @RequestBody CourseScheduleDTO scheduleDTO,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Updating schedule with ID: {} for teacher course: {}", scheduleId, scheduleDTO.getTeacherCourseId());

            // Teachers can only update their own schedules
            if (currentUser.getRole() == Role.ROLE_TEACHER
                    && !courseScheduleService.isTeachersSchedule(currentUser.getId(), scheduleId)) {
                throw new AccessDeniedException("Teachers can only update their own schedules");
            }

            CourseScheduleDTO updatedSchedule = courseScheduleService.updateSchedule(scheduleId, scheduleDTO);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Schedule updated successfully",
                    updatedSchedule
            ));
        } catch (EntityNotFoundException e) {
            log.error("Entity not found while updating schedule: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Entity not found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access denied while updating schedule: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating schedule {}: {}", scheduleId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating schedule: " + e.getMessage());
        }
    }
}