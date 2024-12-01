package com.lsm.controller;

import com.lsm.model.DTOs.ClassEntityRequestDTO;
import com.lsm.model.DTOs.ClassEntityResponseDTO;
import com.lsm.model.entity.ClassEntity;
import com.lsm.service.ClassEntityService;
import com.lsm.mapper.ClassEntityMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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

    @Operation(summary = "Create a new class", description = "Only teachers and admins can create classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Class created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping
    public ResponseEntity<ClassEntityResponseDTO> createClass(@Valid @RequestBody ClassEntityRequestDTO requestDTO) {
        ClassEntity entity = classMapper.toEntity(requestDTO);
        ClassEntity createdClass = classService.createClass(entity, requestDTO.getTeacherId(), requestDTO.getStudentIds());
        return new ResponseEntity<>(classMapper.toDTO(createdClass), HttpStatus.CREATED);
    }

    @Operation(summary = "Get class by ID", description = "Teachers can view their own classes, students can view enrolled classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Class retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ClassEntityResponseDTO> getClassById(@PathVariable Long id) {
        ClassEntity classEntity = classService.getClassById(id);
        return ResponseEntity.ok(classMapper.toDTO(classEntity));
    }

    @Operation(summary = "Get all classes", description = "Admin can view all classes, teachers see their classes, students see enrolled classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping
    public ResponseEntity<List<ClassEntityResponseDTO>> getAllClasses(Authentication authentication) {
        List<ClassEntityResponseDTO> classes = classService.getAllClasses(authentication)
                .stream()
                .map(classMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classes);
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
    public ResponseEntity<ClassEntityResponseDTO> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody ClassEntityRequestDTO requestDTO) {
        ClassEntity entity = classMapper.toEntity(requestDTO);
        ClassEntity updatedClass = classService.updateClass(id, entity, requestDTO.getTeacherId(), requestDTO.getStudentIds());
        return ResponseEntity.ok(classMapper.toDTO(updatedClass));
    }

    @Operation(summary = "Delete a class", description = "Teachers can only delete their own classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Class deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add student to class", description = "Teachers can add students to their own classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student added successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<ClassEntityResponseDTO> addStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        ClassEntity updatedClass = classService.addStudent(classId, studentId);
        return ResponseEntity.ok(classMapper.toDTO(updatedClass));
    }

    @Operation(summary = "Remove student from class", description = "Teachers can remove students from their own classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student removed successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<ClassEntityResponseDTO> removeStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        ClassEntity updatedClass = classService.removeStudent(classId, studentId);
        return ResponseEntity.ok(classMapper.toDTO(updatedClass));
    }

    @Operation(summary = "Get all classes of the teacher", description = "Teachers can view all their classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/teacher")
    public ResponseEntity<List<ClassEntityResponseDTO>> getTeacherClasses(Authentication authentication) {
        List<ClassEntityResponseDTO> classes = classService.getTeacherClasses(authentication)
                .stream()
                .map(classMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(classes);
    }

    @Operation(summary = "Get all classes of the student", description = "Students can view all their enrolled classes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_COORDINATOR')")
    @GetMapping("/student")
    public ResponseEntity<ClassEntityResponseDTO> getStudentClasses(Authentication authentication) {
        ClassEntityResponseDTO classEntity = classMapper.toDTO(classService.getStudentClasses(authentication));
        return ResponseEntity.ok(classEntity);
    }
}