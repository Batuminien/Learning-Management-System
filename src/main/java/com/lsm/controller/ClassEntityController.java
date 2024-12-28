package com.lsm.controller;

import com.lsm.model.DTOs.ClassEntityRequestDTO;
import com.lsm.model.DTOs.ClassEntityResponseDTO;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.service.ClassEntityService;
import com.lsm.mapper.ClassEntityMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/classes")
@Validated
@Tag(name = "Class Management", description = "APIs for managing classes")
@SecurityRequirement(name = "bearerAuth")
public class ClassEntityController {

    private final ClassEntityService classService;
    private final ClassEntityMapper classMapper;

    @Autowired
    public ClassEntityController(ClassEntityService classService, ClassEntityMapper classMapper) {
        this.classService = classService;
        this.classMapper = classMapper;
    }

    @Operation(summary = "Create a new class")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping
    public ResponseEntity<ApiResponse_<ClassEntityResponseDTO>> createClass(
            @Valid @RequestBody ClassEntityRequestDTO requestDTO,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            if (loggedInUser.getRole() == Role.ROLE_STUDENT)
                throw new AccessDeniedException("Students can't create a class");

            ClassEntity entity = classMapper.toEntity(requestDTO);
            ClassEntity createdClass = classService.createClass(loggedInUser, entity,
                    requestDTO.getTeacherCourses(), requestDTO.getStudentIds());

            return new ResponseEntity<>(new ApiResponse_<>(
                    true,
                    "Class created successfully",
                    classMapper.toDTO(createdClass)
            ), HttpStatus.CREATED);
        } catch (AccessDeniedException e) {
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get class by ID", description = "Teachers can view their own classes, students can view enrolled classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Class retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse_<ClassEntityResponseDTO>> getClassById(@PathVariable Long id, Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            ClassEntity classEntity = classService.getClassById(loggedInUser, id);
            ApiResponse_<ClassEntityResponseDTO> response = new ApiResponse_<>(
                    true,
                    "Class retrieved successfully",
                    classMapper.toDTO(classEntity)
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied in getClassById: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in getClassById: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all classes", description = "Admin can view all classes, teachers see their classes, students see enrolled classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping
    public ResponseEntity<ApiResponse_<List<ClassEntityResponseDTO>>> getAllClasses(Authentication authentication) {
        try {
            List<ClassEntityResponseDTO> classes = classService.getAllClassesDTO(authentication);
            ApiResponse_<List<ClassEntityResponseDTO>> response = new ApiResponse_<>(
                    true,
                    "Classes retrieved successfully",
                    classes
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error in getAllClasses: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Update a class", description = "Teachers can only update their own classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Class updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse_<ClassEntityResponseDTO>> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody ClassEntityRequestDTO requestDTO,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            ClassEntity entity = classMapper.toEntity(requestDTO);
            ClassEntity updatedClass = classService.updateClass(loggedInUser, id, entity,
                    requestDTO.getTeacherCourses(), requestDTO.getStudentIds());

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Class updated successfully",
                    classMapper.toDTO(updatedClass)
            ));
        } catch (AccessDeniedException e) {
            log.error("Access denied in updateClass: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in updateClass: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete a class", description = "Teachers can only delete their own classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Class deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse_<Void>> deleteClass(@PathVariable Long id) {
        try {
            classService.deleteClass(id);
            ApiResponse_<Void> response = new ApiResponse_<>(
                    true,
                    "Class deleted successfully",
                    null
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied in deleteClass: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in deleteClass: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Add student to class", description = "Teachers can add students to their own classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student added successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<ApiResponse_<ClassEntityResponseDTO>> addStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            ClassEntity updatedClass = classService.addStudent(loggedInUser, classId, studentId);
            ApiResponse_<ClassEntityResponseDTO> response = new ApiResponse_<>(
                    true,
                    "Student added successfully",
                    classMapper.toDTO(updatedClass)
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied in addStudent: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in addStudent: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Add multiple students to class", description = "Teachers can add multiple students to their own classes in a single request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Students added successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class or one or more students not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping("/{classId}/students/bulk")
    public ResponseEntity<ApiResponse_<ClassEntityResponseDTO>> addStudentsBulk(
            @PathVariable Long classId,
            @Valid @RequestBody List<Long> studentIds,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            ClassEntity updatedClass = classService.addStudentsBulk(loggedInUser, classId, studentIds);
            ApiResponse_<ClassEntityResponseDTO> response = new ApiResponse_<>(
                    true,
                    String.format("Successfully added %d students to the class", studentIds.size()),
                    classMapper.toDTO(updatedClass)
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied while adding students in bulk: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while adding students in bulk: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Remove student from class", description = "Teachers can remove students from their own classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student removed successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<ApiResponse_<ClassEntityResponseDTO>> removeStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            Authentication authentication) {
        try {
            AppUser loggedInUser = (AppUser) authentication.getPrincipal();
            ClassEntity updatedClass = classService.removeStudent(loggedInUser, classId, studentId);
            ApiResponse_<ClassEntityResponseDTO> response = new ApiResponse_<>(
                    true,
                    "Student removed successfully",
                    classMapper.toDTO(updatedClass)
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied in removeStudent: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in removeStudent: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all classes of the teacher", description = "Teachers can view all their classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/teacher")
    public ResponseEntity<ApiResponse_<List<ClassEntityResponseDTO>>> getTeacherClasses(Authentication authentication) {
        try {
            List<ClassEntityResponseDTO> classes = classService.getTeacherClasses(authentication)
                    .stream()
                    .map(classMapper::toDTO)
                    .collect(Collectors.toList());
            ApiResponse_<List<ClassEntityResponseDTO>> response = new ApiResponse_<>(
                    true,
                    "Teacher classes retrieved successfully",
                    classes
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied in getTeacherClasses: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in getTeacherClasses: {}", e.getMessage());
            return ApiResponse_.httpError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all classes of the student", description = "Students can view all their enrolled classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/student")
    public ResponseEntity<ApiResponse_<ClassEntityResponseDTO>> getStudentClasses(Authentication authentication) {
        try {
            ClassEntity classEntity = classService.getStudentClasses(authentication);
            ApiResponse_<ClassEntityResponseDTO> response = new ApiResponse_<>(
                    true,
                    "Student classes retrieved successfully",
                    classMapper.toDTO(classEntity)
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
}