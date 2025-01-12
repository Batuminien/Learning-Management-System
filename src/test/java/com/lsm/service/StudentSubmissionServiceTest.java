package com.lsm.service;

import com.lsm.model.DTOs.SubmitAssignmentDTO;
import com.lsm.model.entity.*;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.AssignmentStatus;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AssignmentRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.repository.StudentSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentSubmissionServiceTest {
/*
    @Mock
    private StudentSubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private ClassEntityRepository classEntityRepository;

    @InjectMocks
    private StudentSubmissionService submissionService;

    @TempDir
    Path tempDir;

    private AppUser student;
    private Assignment assignment;
    private ClassEntity classEntity;
    private Course course;
    private StudentSubmission submission;
    private SubmitAssignmentDTO submitDTO;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // Setup Course
        course = Course.builder()
                .id(1L)
                .name("Test Course")
                .build();

        // Setup Class
        classEntity = ClassEntity.builder()
                .id(1L)
                .name("Test Class")
                .courses(Set.of(course))
                .build();

        // Setup Student
        student = AppUser.builder()
                .id(1L)
                .role(Role.ROLE_STUDENT)
                .studentDetails(StudentDetails.builder()
                        .classEntity(1L)
                        .build())
                .build();

        // Setup Assignment
        assignment = Assignment.builder()
                .id(1L)
                .title("Test Assignment")
                .dueDate(LocalDate.now().plusDays(7))
                .course(course)
                .studentSubmissions(new ArrayList<>())
                .build();

        // Setup Submission
        submission = StudentSubmission.builder()
                .id(1L)
                .student(student)
                .assignment(assignment)
                .status(AssignmentStatus.PENDING)
                .build();

        // Setup MockMultipartFile
        mockFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        // Setup SubmitDTO
        submitDTO = new SubmitAssignmentDTO();
        submitDTO.setDocument(mockFile);
        submitDTO.setSubmissionComment("Test Comment");

        // Set upload directory
        ReflectionTestUtils.setField(submissionService, "UPLOAD_DIR", tempDir.toString());
    }

    @Nested
    @DisplayName("Submit Assignment Tests")
    class SubmitAssignmentTests {

        @Test
        @DisplayName("Should submit assignment successfully")
        void shouldSubmitAssignmentSuccessfully() throws Exception {
            // Arrange
            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
            when(classEntityRepository.findById(1L)).thenReturn(Optional.of(classEntity));
            when(submissionRepository.save(any(StudentSubmission.class))).thenReturn(submission);

            // Act
            StudentSubmission result = submissionService.submitAssignment(1L, submitDTO, student);

            // Assert
            assertNotNull(result);
            assertEquals(AssignmentStatus.SUBMITTED, result.getStatus());
            assertNotNull(result.getDocument());
            verify(submissionRepository).save(any(StudentSubmission.class));
        }

        @Test
        @DisplayName("Should throw exception when assignment not found")
        void shouldThrowExceptionWhenAssignmentNotFound() {
            // Arrange
            when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> submissionService.submitAssignment(1L, submitDTO, student));
        }

        @Test
        @DisplayName("Should throw exception when deadline passed")
        void shouldThrowExceptionWhenDeadlinePassed() {
            // Arrange
            assignment.setDueDate(LocalDate.now().minusDays(1));
            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> submissionService.submitAssignment(1L, submitDTO, student));
        }

        @Test
        @DisplayName("Should throw exception when submission already graded")
        void shouldThrowExceptionWhenSubmissionGraded() {
            // Arrange
            submission.setStatus(AssignmentStatus.GRADED);
            submission.setGrade(85.0);
            assignment.getStudentSubmissions().add(submission);

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
            when(classEntityRepository.findById(1L)).thenReturn(Optional.of(classEntity));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> submissionService.submitAssignment(1L, submitDTO, student));
        }
    }

    @Nested
    @DisplayName("Find Submission Tests")
    class FindSubmissionTests {

        @Test
        @DisplayName("Should find submission by ID successfully")
        void shouldFindSubmissionById() {
            // Arrange
            when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));

            // Act
            StudentSubmission result = submissionService.findSubmissionById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(submission.getId(), result.getId());
        }

        @Test
        @DisplayName("Should find student submission successfully")
        void shouldFindStudentSubmission() {
            // Arrange
            when(submissionRepository.findByAssignment_IdAndStudent_Id(1L, 1L))
                    .thenReturn(Optional.of(submission));

            // Act
            StudentSubmission result = submissionService.findStudentSubmission(1L, 1L);

            // Assert
            assertNotNull(result);
            assertEquals(submission.getId(), result.getId());
        }
    }

    @Nested
    @DisplayName("Delete Submission Tests")
    class DeleteSubmissionTests {

        @Test
        @DisplayName("Should delete submission successfully")
        void shouldDeleteSubmission() throws Exception {
            // Arrange
            AssignmentDocument document = AssignmentDocument.builder()
                    .fileName("test.pdf")
                    .filePath(tempDir.resolve("test.pdf").toString())
                    .uploadTime(LocalDateTime.now())
                    .build();
            submission.setDocument(document);

            when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));

            // Act
            submissionService.deleteSubmission(1L);

            // Assert
            verify(submissionRepository).delete(submission);
        }

        @Test
        @DisplayName("Should throw exception when submission not found for deletion")
        void shouldThrowExceptionWhenSubmissionNotFoundForDeletion() {
            // Arrange
            when(submissionRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> submissionService.deleteSubmission(1L));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate student enrollment")
        void shouldValidateStudentEnrollment() throws Exception {
            // Arrange
            ClassEntity differentClass = ClassEntity.builder()
                    .id(2L)
                    .courses(new HashSet<>())
                    .build();

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
            when(classEntityRepository.findById(1L)).thenReturn(Optional.of(differentClass));

            // Act & Assert
            assertThrows(AccessDeniedException.class,
                    () -> submissionService.submitAssignment(1L, submitDTO, student));
        }

        @Test
        @DisplayName("Should validate submission status")
        void shouldValidateSubmissionStatus() {
            // Arrange
            submission.setStatus(AssignmentStatus.SUBMITTED);
            assignment.getStudentSubmissions().add(submission);

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
            when(classEntityRepository.findById(1L)).thenReturn(Optional.of(classEntity));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> submissionService.submitAssignment(1L, submitDTO, student));
        }
    }
 */
}