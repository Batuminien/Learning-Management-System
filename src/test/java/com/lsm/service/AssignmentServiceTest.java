package com.lsm.service;

import com.lsm.model.DTOs.*;
import com.lsm.model.entity.*;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.AssignmentStatus;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {
/*
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private ClassEntityRepository classEntityRepository;
    @Mock
    private AssignmentDocumentRepository assignmentDocumentRepository;
    @Mock
    private StudentSubmissionRepository studentSubmissionRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private AppUserService appUserService;
    @Mock
    private AssignmentDocumentService assignmentDocumentService;
    @Mock
    private StudentSubmissionService studentSubmissionService;

    @InjectMocks
    private AssignmentService assignmentService;

    private AppUser teacherUser;
    private AppUser studentUser;
    private ClassEntity classEntity;
    private Course course;
    private Assignment assignment;
    private AssignmentRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() {
        // Set up teacher
        teacherUser = AppUser.builder()
                .id(1L)
                .username("teacher")
                .role(Role.ROLE_TEACHER)
                .teacherDetails(TeacherDetails.builder()
                        .classes(new HashSet<>())
                        .build())
                .build();

        // Set up student
        studentUser = AppUser.builder()
                .id(2L)
                .username("student")
                .role(Role.ROLE_STUDENT)
                .studentDetails(StudentDetails.builder()
                        .classEntity(1L)
                        .build())
                .build();

        // Set up class
        classEntity = ClassEntity.builder()
                .id(1L)
                .name("Test Class")
                .build();
        teacherUser.getTeacherDetails().getClasses().add(classEntity);

        // Set up course
        course = Course.builder()
                .id(1L)
                .name("Test Course")
                .build();

        // Set up assignment
        assignment = Assignment.builder()
                .id(1L)
                .title("Test Assignment")
                .description("Test Description")
                .dueDate(LocalDate.now().plusDays(7))
                .assignedBy(teacherUser)
                .classEntity(classEntity)
                .course(course)
                .studentSubmissions(new ArrayList<>())
                .build();

        // Set up valid request DTO
        validRequestDTO = new AssignmentRequestDTO();
        validRequestDTO.setTitle("Test Assignment");
        validRequestDTO.setDescription("Test Description");
        validRequestDTO.setDueDate(LocalDate.now().plusDays(7));
        validRequestDTO.setClassId(1L);
        validRequestDTO.setCourseId(1L);
    }

    @Nested
    @DisplayName("Create Assignment Tests")
    class CreateAssignmentTests {

        @BeforeEach
        void setUpValidationFields() {
            // Inject maxTitleLength into service for testing
            ReflectionTestUtils.setField(assignmentService, "maxTitleLength", 100);
            ReflectionTestUtils.setField(assignmentService, "minDueDateDays", 1);

            // Ensure validRequestDTO has valid values
            validRequestDTO = new AssignmentRequestDTO();
            validRequestDTO.setTitle("Test Assignment"); // Short valid title
            validRequestDTO.setDescription("Test Description");
            validRequestDTO.setDueDate(LocalDate.now().plusDays(7));
            validRequestDTO.setClassId(1L);
            validRequestDTO.setCourseId(1L);
        }

        @Test
        @DisplayName("Should successfully create assignment when valid request")
        void shouldCreateAssignmentSuccessfully() throws AccessDeniedException {
            // Arrange
            when(appUserRepository.findById(1L)).thenReturn(Optional.of(teacherUser));
            when(classEntityRepository.findById(1L)).thenReturn(Optional.of(classEntity));
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);

            // Act
            Assignment result = assignmentService.createAssignment(validRequestDTO, 1L);

            // Assert
            assertNotNull(result);
            assertEquals("Test Assignment", result.getTitle());
            assertEquals("Test Description", result.getDescription());
            verify(assignmentRepository).save(any(Assignment.class));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when student tries to create assignment")
        void shouldThrowAccessDeniedForStudent() {
            // Arrange
            when(appUserRepository.findById(2L)).thenReturn(Optional.of(studentUser));

            // Act & Assert
            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                    () -> assignmentService.createAssignment(validRequestDTO, 2L));

            assertEquals("Students can't create assignments", exception.getMessage());
            verify(classEntityRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when class not found")
        void shouldThrowEntityNotFoundForInvalidClass() {
            // Arrange
            when(appUserRepository.findById(1L)).thenReturn(Optional.of(teacherUser));
            when(classEntityRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> assignmentService.createAssignment(validRequestDTO, 1L));

            assertEquals("Class not found", exception.getMessage());
            verify(courseRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when title exceeds max length")
        void shouldThrowExceptionWhenTitleTooLong() {
            // Arrange
            validRequestDTO.setTitle("A".repeat(101)); // Exceeds maxTitleLength of 100

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> assignmentService.createAssignment(validRequestDTO, 1L));

            assertEquals("Title exceeds maximum length", exception.getMessage());
            verify(appUserRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when due date is too soon")
        void shouldThrowExceptionWhenDueDateTooSoon() {
            // Arrange
            validRequestDTO.setDueDate(LocalDate.now()); // Less than minDueDateDays

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> assignmentService.createAssignment(validRequestDTO, 1L));

            assertEquals("Due date must be at least 1 days in the future", exception.getMessage());
            verify(appUserRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when teacher tries to create assignment for another class")
        void shouldThrowAccessDeniedForWrongClass() {
            // Arrange
            ClassEntity otherClass = ClassEntity.builder()
                    .id(2L)
                    .name("Other Class")
                    .build();

            // Update validRequestDTO to use the otherClass id
            validRequestDTO.setClassId(2L);  // Trying to create assignment for another class

            // Setup mocks
            when(appUserRepository.findById(1L)).thenReturn(Optional.of(teacherUser));
            when(classEntityRepository.findById(2L)).thenReturn(Optional.of(otherClass));
            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            // Act & Assert
            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                    () -> assignmentService.createAssignment(validRequestDTO, 1L));

            assertEquals("Teachers can create assignments only for their assigned classes", exception.getMessage());

            // Verify only the necessary calls
            verify(appUserRepository).findById(1L);
            verify(classEntityRepository).findById(2L);
        }
    }

    @Nested
    @DisplayName("Get Assignments Tests")
    class GetAssignmentsTests {

        @Test
        @DisplayName("Should return teacher's assignments")
        void shouldGetTeacherAssignments() throws AccessDeniedException {
            // Arrange
            List<Assignment> assignments = Collections.singletonList(assignment);
            when(appUserRepository.findById(1L)).thenReturn(Optional.of(teacherUser));
            when(assignmentRepository.findByAssignedByOrderByDueDateDesc(teacherUser))
                    .thenReturn(assignments);

            // Act
            List<AssignmentDTO> result = assignmentService.getAssignmentsByTeacher(1L, null, null, null, teacherUser);

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals("Test Assignment", result.get(0).getTitle());
        }

        @Test
        @DisplayName("Should return student's assignments")
        void shouldGetStudentAssignments() throws AccessDeniedException {
            // Arrange
            Set<Assignment> assignments = Collections.singleton(assignment);
            when(appUserRepository.findById(2L)).thenReturn(Optional.of(studentUser));
            when(classEntityRepository.findById(1L)).thenReturn(Optional.of(classEntity));
            when(assignmentRepository.findByClassEntityOrderByDueDateDesc(classEntity))
                    .thenReturn(assignments);

            // Act
            List<StudentAssignmentViewDTO> result = assignmentService.getAssignmentsByStudent(2L);

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Submit Assignment Tests")
    class SubmitAssignmentTests {

        @Test
        @DisplayName("Should successfully submit assignment")
        void shouldSubmitAssignmentSuccessfully() throws Exception {
            // Arrange
            SubmitAssignmentDTO submitDTO = new SubmitAssignmentDTO();

            // Set up class with courses
            classEntity = ClassEntity.builder()
                    .id(1L)
                    .name("Test Class")
                    .courses(new HashSet<>())
                    .build();
            classEntity.getCourses().add(course);

            // Update assignment with properly setup class and studentSubmissions list
            assignment = Assignment.builder()
                    .id(1L)
                    .title("Test Assignment")
                    .description("Test Description")
                    .dueDate(LocalDate.now().plusDays(7))
                    .assignedBy(teacherUser)
                    .classEntity(classEntity)
                    .course(course)
                    .studentSubmissions(new ArrayList<>())
                    .build();

            // Create submission with SUBMITTED status
            StudentSubmission submission = StudentSubmission.builder()
                    .id(1L)
                    .student(studentUser)
                    .assignment(assignment)
                    .status(AssignmentStatus.SUBMITTED)  // Ensure status is SUBMITTED
                    .submissionDate(LocalDate.now())
                    .build();

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
            when(classEntityRepository.findById(1L)).thenReturn(Optional.of(classEntity));
            when(studentSubmissionService.submitAssignment(eq(1L), any(SubmitAssignmentDTO.class), eq(studentUser)))
                    .thenReturn(submission);
            when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);

            // Act
            StudentSubmission result = assignmentService.submitAssignment(1L, submitDTO, studentUser);

            // Assert
            assertNotNull(result);
            assertEquals(AssignmentStatus.SUBMITTED, result.getStatus());
            verify(assignmentRepository).save(any(Assignment.class));
            verify(studentSubmissionService).submitAssignment(eq(1L), any(SubmitAssignmentDTO.class), eq(studentUser));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when submitting after deadline")
        void shouldThrowExceptionWhenSubmittingLate() {
            // Arrange
            assignment.setDueDate(LocalDate.now().minusDays(1));
            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> assignmentService.submitAssignment(1L, new SubmitAssignmentDTO(), studentUser));
        }
    }

    @Nested
    @DisplayName("Grade Assignment Tests")
    class GradeAssignmentTests {

        @Test
        @DisplayName("Should successfully grade assignment")
        void shouldGradeAssignmentSuccessfully() throws AccessDeniedException {
            // Arrange
            GradeDTO gradeDTO = new GradeDTO();
            gradeDTO.setGrade(85.0);
            gradeDTO.setFeedback("Good work");

            StudentSubmission submission = StudentSubmission.builder()
                    .student(studentUser)
                    .status(AssignmentStatus.SUBMITTED)
                    .build();
            assignment.getStudentSubmissions().add(submission);

            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
            when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);

            // Act
            Assignment result = assignmentService.gradeAssignment(1L, gradeDTO, teacherUser, 2L);

            // Assert
            assertNotNull(result);
            assertEquals(AssignmentStatus.GRADED,
                    result.getStudentSubmissions().get(0).getStatus());
            assertEquals(85.0, result.getStudentSubmissions().get(0).getGrade());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when student tries to grade")
        void shouldThrowAccessDeniedWhenStudentGrades() {
            // Arrange
            when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

            // Act & Assert
            assertThrows(AccessDeniedException.class,
                    () -> assignmentService.gradeAssignment(1L, new GradeDTO(), studentUser, 2L));
        }
    }

 */
}