package com.lsm.controller;

import com.lsm.model.DTOs.ClassEntityResponseDTO;
import com.lsm.model.DTOs.CourseDTO;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.service.CourseService;

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
@RequestMapping("/api/v1/courses")
@Tag(name = "Course Management", description = "APIs for managing courses")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Retrieve a specific course by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ApiResponse_<CourseDTO>> getCourseById(
            @Parameter(description = "ID of the course", required = true)
            @PathVariable @Positive Long id,
            Authentication authentication
    ) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            log.info("Retrieving course with ID: {}", id);
            CourseDTO courseDTO = courseService.getCourseById(loggedInUser, id);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Course retrieved successfully",
                    courseDTO
            ));
        } catch (EntityNotFoundException e) {
            log.error("Course not found with ID in getCourseById({}): {}", id, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Course not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving course with ID {}: {}", id, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving course: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Get all courses", description = "Retrieve all courses in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    public ResponseEntity<ApiResponse_<List<CourseDTO>>> getAllCourses() {
        try {
            log.info("Retrieving all courses");
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "All courses retrieved successfully",
                    courseService.getAllCourses()
            ));
        } catch (Exception e) {
            log.error("Error retrieving all courses: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving courses: " + e.getMessage());
        }
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "Get courses by class ID", description = "Retrieve all courses for a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ApiResponse_<List<CourseDTO>>> getCoursesByClassId(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable @Positive Long classId
    ) {
        try {
            log.info("Retrieving courses for class ID: {}", classId);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Courses for class retrieved successfully",
                    courseService.getCoursesByClassId(classId)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Class not found with ID {}: {}", classId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Class not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving courses for class {}: {}", classId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving courses: " + e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get courses by student", description = "Retrieve all courses a student is enrolled in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ApiResponse_<List<CourseDTO>>> getCoursesByStudent(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Positive Long studentId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Retrieving courses for student ID: {}", studentId);
            log.info("Retrieving courses for loggedInUser ID: {}", currentUser.getId());

            // Students can only view their own courses
            if (currentUser.getRole().equals(Role.ROLE_STUDENT) && !currentUser.getId().equals(studentId)) {
                throw new AccessDeniedException("Students can only view their own courses");
            }

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Student courses retrieved successfully",
                    courseService.getCoursesByStudent(studentId)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Student not found with ID {}: {}", studentId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Student not found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access denied for student courses: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving courses for student {}: {}", studentId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving courses: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "Create a new course", description = "Create a new course in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Course created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    public ResponseEntity<ApiResponse_<CourseDTO>> createCourse(
            @Valid @RequestBody CourseDTO courseDTO
    ) {
        try {
            log.info("Creating new course: {}", courseDTO.getName());
            CourseDTO createdCourse = courseService.createCourse(courseDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse_<>(
                            true,
                            "Course created successfully",
                            createdCourse
                    ));
        } catch (Exception e) {
            log.error("Error creating course: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, "Error creating course: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course", description = "Update an existing course in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    public ResponseEntity<ApiResponse_<CourseDTO>> updateCourse(
            @Parameter(description = "ID of the course to update", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody CourseDTO courseDTO
    ) {
        try {
            log.info("Updating course with ID: {}", id);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Course updated successfully",
                    courseService.updateCourse(id, courseDTO)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Course not found with ID {} while updating course: {}", id, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Course not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating course {}: {}", id, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, "Error updating course: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a course", description = "Delete an existing course from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    public ResponseEntity<ApiResponse_<Void>> deleteCourse(
            @Parameter(description = "ID of the course to delete", required = true)
            @PathVariable @Positive Long id
    ) {
        try {
            log.info("Deleting course with ID: {}", id);
            courseService.deleteCourse(id);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Course deleted successfully",
                    null
            ));
        } catch (EntityNotFoundException e) {
            log.error("Course not found with ID {} while deleting course: {}", id, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Course not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting course {}: {}", id, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting course: " + e.getMessage());
        }
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get courses by teacher", description = "Retrieve all courses assigned to a specific teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse_<List<CourseDTO>>> getCoursesByTeacher(
            @Parameter(description = "ID of the teacher", required = true)
            @PathVariable @Positive Long teacherId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();
            log.info("Retrieving courses for teacher ID: {}", teacherId);

            // Teachers can only view their own courses
            if (currentUser.getRole() == Role.ROLE_TEACHER && !currentUser.getId().equals(teacherId)) {
                throw new AccessDeniedException("Teachers can only view their own courses");
            }

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Teacher courses retrieved successfully",
                    courseService.getCoursesByTeacher(teacherId)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Teacher not found with ID {}: {}", teacherId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, "Teacher not found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access denied for teacher courses: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving courses for teacher {}: {}", teacherId, e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving courses: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search courses", description = "Search courses by name, code, or description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<ApiResponse_<List<CourseDTO>>> searchCourses(
            @Parameter(description = "Search query")
            @RequestParam(required = false) String query,
            @Parameter(description = "Filter by semester")
            @RequestParam(required = false) String semester,
            @Parameter(description = "Filter by year")
            @RequestParam(required = false) Integer year
    ) {
        try {
            log.info("Searching courses with query: {}, semester: {}, year: {}", query, semester, year);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Search results retrieved successfully",
                    courseService.searchCourses(query, semester, year)
            ));
        } catch (Exception e) {
            log.error("Error searching courses: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error searching courses: " + e.getMessage());
        }
    }

    @PutMapping("/{courseId}/teacher/{teacherId}")
    @Operation(summary = "Assign teacher to course", description = "Assign a teacher to a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher assigned successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Course or teacher not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    public ResponseEntity<ApiResponse_<CourseDTO>> assignTeacherToCourse(
            @Parameter(description = "ID of the course", required = true)
            @PathVariable @Positive Long courseId,
            @Parameter(description = "ID of the teacher", required = true)
            @PathVariable @Positive Long teacherId,
            @RequestBody List<Long> classIds
    ) {
        try {
            log.info("Assigning teacher {} to course {}", teacherId, courseId);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Teacher assigned successfully",
                    courseService.assignTeacherToCourse(courseId, teacherId, classIds)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Error assigning teacher to course: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error assigning teacher: " + e.getMessage());
        }
    }

    @DeleteMapping("/{courseId}/teacher/{teacherId}")
    @Operation(summary = "Remove teacher from course", description = "Remove the assigned teacher from a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher removed successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    public ResponseEntity<ApiResponse_<CourseDTO>> removeTeacherFromCourse(
            @Parameter(description = "ID of the course", required = true)
            @PathVariable @Positive Long courseId,
            @PathVariable @Positive Long teacherId
    ) {
        try {
            log.info("Removing teacher from course {}", courseId);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Teacher removed successfully",
                    courseService.removeTeacherFromCourse(courseId, teacherId)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Course not found: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Error removing teacher from course: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing teacher: " + e.getMessage());
        }
    }

    @PutMapping("/{courseId}/teacher/{teacherId}/update")
    @Operation(summary = "Update course teacher", description = "Update the teacher assigned to a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher updated successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Course or teacher not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    public ResponseEntity<ApiResponse_<CourseDTO>> updateCourseTeacher(
            @Parameter(description = "ID of the course", required = true)
            @PathVariable @Positive Long courseId,
            @Parameter(description = "ID of the new teacher", required = true)
            @PathVariable @Positive Long teacherId,
            @RequestBody List<Long> classIds
    ) {
        try {
            // TODO: loggedInUser check
            log.info("Updating teacher for course {} to {}", courseId, teacherId);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Teacher updated successfully",
                    courseService.updateCourseTeacher(courseId, teacherId, classIds)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Resource not found in updateCourseTeacher: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating course teacher: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating teacher: " + e.getMessage());
        }
    }

    @GetMapping("/{courseId}/teacher/{teacherId}/classes")
    @Operation(summary = "Get classes for a teacher-course combination",
            description = "Retrieve all classes assigned to a specific teacher for a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Course or teacher not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse_<List<ClassEntityResponseDTO>>> getTeacherCourseClasses(
            @Parameter(description = "ID of the course", required = true)
            @PathVariable @Positive Long courseId,
            @Parameter(description = "ID of the teacher", required = true)
            @PathVariable @Positive Long teacherId,
            Authentication authentication
    ) {
        try {
            AppUser currentUser = (AppUser) authentication.getPrincipal();

            // Teachers can only view their own classes
            if (currentUser.getRole() == Role.ROLE_TEACHER && !currentUser.getId().equals(teacherId)) {
                throw new AccessDeniedException("Teachers can only view their own classes");
            }

            log.info("Retrieving classes for teacher {} in course {}", teacherId, courseId);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Classes retrieved successfully",
                    courseService.getTeacherCourseClasses(courseId, teacherId)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Resource not found in get teacher course classes: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access denied: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving classes: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving classes: " + e.getMessage());
        }
    }

    @PutMapping("/{courseId}/teacher/{teacherId}/classes")
    @Operation(summary = "Update classes for a teacher-course combination",
            description = "Update the classes assigned to a specific teacher for a course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classes updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Course, teacher or classes not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    public ResponseEntity<ApiResponse_<CourseDTO>> updateTeacherCourseClasses(
            @Parameter(description = "ID of the course", required = true)
            @PathVariable @Positive Long courseId,
            @Parameter(description = "ID of the teacher", required = true)
            @PathVariable @Positive Long teacherId,
            @RequestBody List<Long> classIds
    ) {
        try {
            log.info("Updating classes for teacher {} in course {} with classes {}", teacherId, courseId, classIds);
            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Classes updated successfully",
                    courseService.updateTeacherCourseClasses(courseId, teacherId, classIds)
            ));
        } catch (EntityNotFoundException e) {
            log.error("Resource not found in update teacher course classes: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error updating classes: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating classes: " + e.getMessage());
        }
    }
}