package com.lsm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lsm.model.DTOs.*;
import com.lsm.model.entity.*;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.service.AssignmentDocumentService;
import com.lsm.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private AssignmentDocumentService documentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AssignmentController assignmentController;

    private AppUser teacherUser;
    private AppUser studentUser;
    private AppUser adminUser;
    private Assignment testAssignment;
    private AssignmentRequestDTO requestDTO;
    private AssignmentDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper for Java 8 date/time types
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create MockMvc with the configured ObjectMapper
        mockMvc = MockMvcBuilders.standaloneSetup(assignmentController)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper),
                        new ResourceHttpMessageConverter(),
                        new ByteArrayHttpMessageConverter()
                )
                .build();

        // Setup users
        teacherUser = AppUser.builder()
                .id(1L)
                .username("teacher")
                .role(Role.ROLE_TEACHER)
                .build();

        studentUser = AppUser.builder()
                .id(2L)
                .username("student")
                .role(Role.ROLE_STUDENT)
                .build();

        adminUser = AppUser.builder()
                .id(3L)
                .username("admin")
                .role(Role.ROLE_ADMIN)
                .build();

        // Setup test assignment
        testAssignment = Assignment.builder()
                .id(1L)
                .title("Test Assignment")
                .description("Test Description")
                .dueDate(LocalDate.now().plusDays(7))
                .date(LocalDate.now())
                .assignedBy(teacherUser)
                .studentSubmissions(new ArrayList<>())
                .classEntity(ClassEntity.builder()
                        .id(1L)
                        .name("Test Class")
                        .build())
                .course(Course.builder()
                        .id(1L)
                        .name("Test Course")
                        .build())
                .build();

        // Setup request DTO
        requestDTO = AssignmentRequestDTO.builder()
                .teacherId(1L)
                .title("Test Assignment")
                .description("Test Description")
                .dueDate(LocalDate.now().plusDays(7))
                .classId(1L)
                .courseId(1L)
                .build();

        // Setup response DTO
        responseDTO = new AssignmentDTO(testAssignment, "Test message");
    }

    @Test
    void createAssignment_AsTeacher_ShouldCreateAssignment() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(assignmentService.createAssignment(any(AssignmentRequestDTO.class), eq(1L)))
                .thenReturn(testAssignment);

        // Act & Assert
        mockMvc.perform(post("/api/v1/assignments/createAssignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Assignment"));
    }

    @Test
    void createAssignment_AsStudent_ShouldReturnForbidden() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(studentUser); // Mock authenticated student
        when(assignmentService.createAssignment(any(AssignmentRequestDTO.class), eq(2L)))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Students cannot create assignments"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/assignments/createAssignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .principal(authentication)) // Attach authentication
                .andExpect(status().isForbidden()) // Expect 403 Forbidden
                .andExpect(jsonPath("$.success").value(false)) // Check response structure
                .andExpect(jsonPath("$.message").value(containsString("Access denied"))); // Validate message
    }

    @Test
    void getTeacherAssignments_AsTeacher_ShouldReturnAssignments() throws Exception {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(assignmentService.getAssignmentsByTeacher(eq(1L), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(responseDTO));

        // Act & Assert
        mockMvc.perform(get("/api/v1/assignments/teacher/1")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Test Assignment"));
    }

    @Test
    void submitAssignment_AsStudent_ShouldSubmitAssignment() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "document",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test content".getBytes()
        );

        StudentSubmission submission = StudentSubmission.builder()
                .id(1L)
                .student(studentUser)
                .assignment(testAssignment)
                .submissionDate(LocalDateTime.now())
                .build();

        when(authentication.getPrincipal()).thenReturn(studentUser);
        when(assignmentService.submitAssignment(eq(1L), any(SubmitAssignmentDTO.class), eq(studentUser)))
                .thenReturn(submission);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/assignments/1/submit")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }


    @Test
    void gradeAssignment_AsTeacher_ShouldGradeAssignment() throws Exception {
        // Arrange
        GradeDTO gradeDTO = new GradeDTO();
        gradeDTO.setGrade(85.0);
        gradeDTO.setFeedback("Good work");

        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(assignmentService.gradeAssignment(eq(1L), any(GradeDTO.class), eq(teacherUser), eq(2L)))
                .thenReturn(testAssignment);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/assignments/1/2/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeDTO))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void downloadDocument_ShouldReturnDocument() throws Exception {
        // Arrange
        byte[] fileContent = "test content".getBytes();
        Resource mockResource = mock(Resource.class);
        when(mockResource.getFilename()).thenReturn("test.pdf");
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
        when(mockResource.contentLength()).thenReturn((long) fileContent.length);
        when(authentication.getPrincipal()).thenReturn(studentUser);
        when(documentService.downloadDocument(eq(1L), any(AppUser.class)))
                .thenReturn(mockResource);

        // Act & Assert
        mockMvc.perform(get("/api/v1/assignments/documents/1")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=\"test.pdf\"")));
    }

    @Test
    void uploadDocument_AsTeacher_ShouldUploadDocument() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test content".getBytes()
        );

        AssignmentDocument document = AssignmentDocument.builder()
                .id(1L)
                .fileName("test.pdf")
                .fileType(MediaType.APPLICATION_PDF_VALUE)
                .fileSize(file.getSize())
                .uploadedBy(teacherUser)
                .uploadTime(LocalDateTime.now())
                .assignment(testAssignment)  // Add the missing assignment
                .build();

        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(documentService.uploadDocument(any(), eq(1L), eq(teacherUser)))
                .thenReturn(document);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/assignments/1/documents")
                        .file(file)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}