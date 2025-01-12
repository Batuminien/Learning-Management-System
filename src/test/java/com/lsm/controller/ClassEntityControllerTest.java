package com.lsm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsm.mapper.ClassEntityMapper;
import com.lsm.model.DTOs.ClassEntityRequestDTO;
import com.lsm.model.DTOs.ClassEntityResponseDTO;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.service.ClassEntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClassEntityControllerTest {
/*
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ClassEntityService classService;

    @Mock
    private ClassEntityMapper classMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ClassEntityController classController;

    private AppUser adminUser;
    private AppUser teacherUser;
    private AppUser studentUser;
    private ClassEntity testClass;
    private ClassEntityRequestDTO requestDTO;
    private ClassEntityResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(classController)
                .build();
        objectMapper = new ObjectMapper();

        // Setup users
        adminUser = AppUser.builder()
                .id(1L)
                .role(Role.ROLE_ADMIN)
                .build();

        teacherUser = AppUser.builder()
                .id(2L)
                .role(Role.ROLE_TEACHER)
                .build();

        studentUser = AppUser.builder()
                .id(3L)
                .role(Role.ROLE_STUDENT)
                .build();

        // Setup test class
        testClass = ClassEntity.builder()
                .id(1L)
                .name("Test Class")
                .description("Test Description")
                .build();

        // Setup DTOs
        requestDTO = ClassEntityRequestDTO.builder()
                .name("Test Class")
                .description("Test Description")
                .teacherId(2L)
                .studentIds(Arrays.asList(3L, 4L))
                .build();

        Map<Long, String> studentMap = new HashMap<>();
        studentMap.put(3L, "Student 1");
        studentMap.put(4L, "Student 2");

        responseDTO = ClassEntityResponseDTO.builder()
                .id(1L)
                .name("Test Class")
                .description("Test Description")
                .teacherId(2L)
                .studentIdAndNames(studentMap)
                .build();
    }

    @Test
    void createClass_WithValidTeacher_ShouldCreateClass() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(classMapper.toEntity(any(ClassEntityRequestDTO.class))).thenReturn(testClass);
        when(classService.createClass(eq(teacherUser), any(), eq(2L), any())).thenReturn(testClass);
        when(classMapper.toDTO(any(ClassEntity.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Class"));
    }

    @Test
    void createClass_WithStudent_ShouldReturnForbidden() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(studentUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .principal(authentication))
                .andExpect(status().isForbidden());
    }

    @Test
    void getClassById_AsTeacher_ShouldReturnClass() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(classService.getClassById(eq(teacherUser), eq(1L))).thenReturn(testClass);
        when(classMapper.toDTO(any(ClassEntity.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/classes/1")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getClassById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(classService.getClassById(eq(teacherUser), eq(999L)))
                .thenThrow(new EntityNotFoundException("Class not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/classes/999")
                        .principal(authentication))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllClasses_AsAdmin_ShouldReturnAllClasses() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(classService.getAllClasses(any(Authentication.class)))
                .thenReturn(Collections.singletonList(testClass));
        when(classMapper.toDTO(any(ClassEntity.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/classes")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void updateClass_WithValidData_ShouldUpdateClass() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(classMapper.toEntity(any(ClassEntityRequestDTO.class))).thenReturn(testClass);
        when(classService.updateClass(eq(teacherUser), eq(1L), any(), eq(2L), any())).thenReturn(testClass);
        when(classMapper.toDTO(any(ClassEntity.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/classes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void addStudent_AsTeacher_ShouldAddStudent() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(classService.addStudent(eq(teacherUser), eq(1L), eq(3L))).thenReturn(testClass);
        when(classMapper.toDTO(any(ClassEntity.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/classes/1/students/3")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void addStudentsBulk_AsTeacher_ShouldAddMultipleStudents() throws Exception {
        // Arrange
        List<Long> studentIds = Arrays.asList(3L, 4L);
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(classService.addStudentsBulk(eq(teacherUser), eq(1L), any())).thenReturn(testClass);
        when(classMapper.toDTO(any(ClassEntity.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/classes/1/students/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentIds))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void removeStudent_AsTeacher_ShouldRemoveStudent() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(classService.removeStudent(eq(teacherUser), eq(1L), eq(3L))).thenReturn(testClass);
        when(classMapper.toDTO(any(ClassEntity.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/classes/1/students/3")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getTeacherClasses_AsTeacher_ShouldReturnClasses() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(classService.getTeacherClasses(any(Authentication.class)))
                .thenReturn(Collections.singletonList(testClass));
        when(classMapper.toDTO(any(ClassEntity.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/classes/teacher")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getStudentClasses_AsStudent_ShouldReturnClasses() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(studentUser);
        when(classService.getStudentClasses(any(Authentication.class))).thenReturn(testClass);
        when(classMapper.toDTO(any(ClassEntity.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/classes/student")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

 */
}