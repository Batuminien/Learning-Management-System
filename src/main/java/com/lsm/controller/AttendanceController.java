package com.lsm.controller;

import com.lsm.model.DTOs.AttendanceDTO;
import com.lsm.model.DTOs.AttendanceStatsDTO;
import com.lsm.model.DTOs.AttendanceRequestDTO;
import com.lsm.model.entity.Attendance;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Validated
@Tag(name = "Attendance Management", description = "APIs for managing student attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(
            summary = "Mark student attendance",
            description = "Allows teachers and administrators to mark attendance for a student"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attendance marked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping("/mark")
    public ResponseEntity<ApiResponse_<AttendanceDTO>> markAttendance(
            @Valid @RequestBody AttendanceRequestDTO attendanceRequest,
            Authentication authentication
    ) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            Attendance attendance = attendanceService.markAttendance(loggedInUser, attendanceRequest);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse_<>(
                            true,
                            "Attendance marked successfully",
                            new AttendanceDTO(attendance, "")
                    ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while marking attendance: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get student attendance records",
            description = "Retrieve attendance records for a specific student. Students can only access their own records."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse_<List<AttendanceDTO>>> getAttendanceByStudentId(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Positive Long studentId,
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) LocalDate endDate,
            Authentication authentication
    ) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            List<AttendanceDTO> attendanceRecords = attendanceService.getAttendanceByStudentId(loggedInUser, studentId, startDate, endDate);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Attendance records retrieved successfully",
                    attendanceRecords
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while getting attendance of the student: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get attendance statistics",
            description = "Retrieve attendance statistics for a specific student"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Student/Class not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/stats/student/{studentId}")
    public ResponseEntity<ApiResponse_<List<AttendanceStatsDTO>>> getAttendanceStatsOfTheStudent(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Positive Long studentId,
            @Parameter(description = "ID of the class")
            @RequestParam Long classId,
            Authentication authentication
    ) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            if (loggedInUser.getRole().equals(Role.ROLE_STUDENT) && !loggedInUser.getId().equals(studentId))
                throw new AccessDeniedException("The student can't view other students stats");
            List<AttendanceStatsDTO> stats = attendanceService.getAttendanceStatsByStudent(loggedInUser, studentId, classId);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Attendance statistics retrieved successfully",
                    stats
            ));
        } catch (AccessDeniedException e) {
            log.error("To display stats of the student as a student, you must be that student: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get attendance statistics",
            description = "Retrieve attendance statistics for a specific student within a date range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Student/Class not found"),
            @ApiResponse(responseCode = "400", description = "Invalid date format or range")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/stats/course/{courseId}")
    public ResponseEntity<ApiResponse_<List<AttendanceStatsDTO>>> getAttendanceStatsOfTheCourse(
            @Parameter(description = "ID of the course", required = true)
            @PathVariable @Positive Long courseId,
            @Parameter(description = "ID of the class")
            @RequestParam(required = false) Long classId,
            @Parameter(description = "Start date (format: yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();

            // Validate date range if provided
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, "Start date must be before or equal to end date");
            }

            List<AttendanceStatsDTO> stats = attendanceService.getAttendanceStatsByCourse(
                    loggedInUser, courseId, classId, startDate, endDate);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Attendance statistics retrieved successfully",
                    stats
            ));
        } catch (AccessDeniedException e) {
            log.error("Error in getAttendanceStatsOfTheCourse: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Bulk mark attendance",
            description = "Mark attendance for multiple students at once"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attendance marked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse_<Integer>> markBulkAttendance(
            @Valid @RequestBody List<AttendanceRequestDTO> attendanceRequests,
            Authentication authentication
    ) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            attendanceService.markBulkAttendance(loggedInUser, attendanceRequests);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse_<>(
                            true,
                            "Bulk attendance marked successfully",
                            attendanceRequests.size()
                    ));
        } catch (AccessDeniedException e) {
            log.error("Error in mark attendance: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Update student attendance",
            description = "Allows teachers and administrators to update an existing attendance record"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attendance updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Attendance record not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PutMapping("/{attendanceId}")
    public ResponseEntity<ApiResponse_<AttendanceDTO>> updateAttendance(
            @Parameter(description = "ID of the attendance record", required = true)
            @PathVariable @Positive Long attendanceId,
            @Valid @RequestBody AttendanceRequestDTO attendanceRequest,
            Authentication authentication
    ) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            Attendance updatedAttendance = attendanceService.updateAttendance(loggedInUser, attendanceId, attendanceRequest);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Attendance updated successfully",
                    new AttendanceDTO(updatedAttendance, "")
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied while updating attendance: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Error updating attendance: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}