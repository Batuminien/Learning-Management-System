package com.lsm.service;

import com.lsm.model.DTOs.AttendanceDTO;
import com.lsm.model.DTOs.AttendanceRequestDTO;
import com.lsm.model.DTOs.AttendanceStatsDTO;
import com.lsm.model.entity.Attendance;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.Course;
import com.lsm.model.entity.TeacherDetails;
import com.lsm.model.entity.StudentDetails;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.AttendanceStatus;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AttendanceRepository;
import com.lsm.repository.AppUserRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {
/*
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ClassEntityRepository classEntityRepository;
    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private AttendanceService attendanceService;

    private AppUser teacherUser;
    private AppUser studentUser;
    private AppUser adminUser;
    private ClassEntity classEntity;
    private Course course;
    private Attendance attendance;
    private AttendanceRequestDTO attendanceRequest;

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
                .courses(new HashSet<>(Collections.singletonList(course)))
                .build();

        // Setup Teacher
        teacherUser = AppUser.builder()
                .id(1L)
                .name("Test Teacher")
                .role(Role.ROLE_TEACHER)
                .teacherDetails(TeacherDetails.builder()
                        .classes(new HashSet<>(Collections.singletonList(classEntity)))
                        .build())
                .build();

        // Setup Student
        studentUser = AppUser.builder()
                .id(2L)
                .name("Test Student")
                .role(Role.ROLE_STUDENT)
                .studentDetails(StudentDetails.builder()
                        .classEntity(1L)
                        .build())
                .build();

        // Setup Admin
        adminUser = AppUser.builder()
                .id(3L)
                .name("Test Admin")
                .role(Role.ROLE_ADMIN)
                .build();

        // Setup Attendance
        attendance = Attendance.builder()
                .id(1L)
                .student(studentUser)
                .date(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .classId(classEntity.getId())
                .courseId(course.getId())
                .build();

        // Setup Attendance Request
        attendanceRequest = AttendanceRequestDTO.builder()
                .studentId(studentUser.getId())
                .date(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .classId(classEntity.getId())
                .courseId(course.getId())
                .build();
    }

    @Nested
    @DisplayName("Mark Attendance Tests")
    class MarkAttendanceTests {

        @Test
        @DisplayName("Should allow teacher to mark attendance")
        void shouldAllowTeacherToMarkAttendance() throws AccessDeniedException {
            // Arrange
            when(appUserService.getCurrentUserWithDetails(teacherUser.getId())).thenReturn(teacherUser);
            when(appUserRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
            when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

            // Act
            Attendance result = attendanceService.markAttendance(teacherUser, attendanceRequest);

            // Assert
            assertNotNull(result);
            assertEquals(attendance.getStatus(), result.getStatus());
            verify(attendanceRepository).save(any(Attendance.class));
        }

        @Test
        @DisplayName("Should not allow student to mark attendance")
        void shouldNotAllowStudentToMarkAttendance() {
            // Act & Assert
            assertThrows(AccessDeniedException.class,
                    () -> attendanceService.markAttendance(studentUser, attendanceRequest));
        }

        @Test
        @DisplayName("Should allow admin to mark attendance")
        void shouldAllowAdminToMarkAttendance() throws AccessDeniedException {
            // Arrange
            when(appUserRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
            when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

            // Act
            Attendance result = attendanceService.markAttendance(adminUser, attendanceRequest);

            // Assert
            assertNotNull(result);
            verify(attendanceRepository).save(any(Attendance.class));
        }
    }

    @Nested
    @DisplayName("Bulk Attendance Tests")
    class BulkAttendanceTests {

        @Test
        @DisplayName("Should mark bulk attendance successfully")
        void shouldMarkBulkAttendanceSuccessfully() throws AccessDeniedException {
            // Arrange
            List<AttendanceRequestDTO> requests = Collections.singletonList(attendanceRequest);
            when(appUserService.getCurrentUserWithDetails(teacherUser.getId())).thenReturn(teacherUser);
            when(appUserRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));

            // Act
            attendanceService.markBulkAttendance(teacherUser, requests);

            // Assert
            verify(attendanceRepository).save(any(Attendance.class));
        }

        @Test
        @DisplayName("Should not allow student to mark bulk attendance")
        void shouldNotAllowStudentToMarkBulkAttendance() {
            // Arrange
            List<AttendanceRequestDTO> requests = Collections.singletonList(attendanceRequest);

            // Act & Assert
            assertThrows(AccessDeniedException.class,
                    () -> attendanceService.markBulkAttendance(studentUser, requests));
        }
    }

    @Nested
    @DisplayName("Get Attendance Tests")
    class GetAttendanceTests {

        @Test
        @DisplayName("Should get student attendance")
        void shouldGetStudentAttendance() throws AccessDeniedException {
            // Arrange
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            when(attendanceRepository.findByStudentId(studentUser.getId()))
                    .thenReturn(Collections.singletonList(attendance));

            // Act
            List<AttendanceDTO> result = attendanceService.getAttendanceByStudentId(
                    studentUser, studentUser.getId(), startDate, endDate);

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(attendance.getStudent().getId(), result.get(0).getStudentId());
        }

        @Test
        @DisplayName("Should not allow student to view other student's attendance")
        void shouldNotAllowStudentToViewOthersAttendance() {
            // Arrange
            AppUser otherStudent = AppUser.builder()
                    .id(4L)
                    .role(Role.ROLE_STUDENT)
                    .build();

            // Act & Assert
            assertThrows(AccessDeniedException.class,
                    () -> attendanceService.getAttendanceByStudentId(
                            otherStudent, studentUser.getId(), null, null));
        }
    }

    @Nested
    @DisplayName("Attendance Stats Tests")
    class AttendanceStatsTests {

        @Test
        @DisplayName("Should get attendance stats by student")
        void shouldGetAttendanceStatsByStudent() throws AccessDeniedException {
            // Arrange
            when(appUserRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
            when(classEntityRepository.getClassEntityById(classEntity.getId())).thenReturn(Optional.of(classEntity));
            when(attendanceRepository.findByStudentIdAndClassId(studentUser.getId(), classEntity.getId()))
                    .thenReturn(Collections.singletonList(attendance));

            // Act
            List<AttendanceStatsDTO> result = attendanceService.getAttendanceStatsByStudent(
                    studentUser, studentUser.getId(), classEntity.getId());

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(studentUser.getId(), result.get(0).getStudentId());
        }

        @Test
        @DisplayName("Should get attendance stats by course")
        void shouldGetAttendanceStatsByCourse() throws AccessDeniedException {
            // Arrange
            AppUser student = AppUser.builder()
                    .id(1L)
                    .name("Test Student")
                    .role(Role.ROLE_STUDENT)
                    .build();

            // Build attendance with proper student reference
            attendance = Attendance.builder()
                    .id(1L)
                    .student(student)
                    .courseId(course.getId())
                    .classId(classEntity.getId())
                    .status(AttendanceStatus.PRESENT)
                    .build();

            when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
            when(classEntityRepository.getClassEntityById(classEntity.getId())).thenReturn(Optional.of(classEntity));
            when(attendanceRepository.findByCourseIdAndClassId(course.getId(), classEntity.getId()))
                    .thenReturn(Collections.singletonList(attendance));
            // Add mock for student lookup
            when(appUserRepository.findById(student.getId())).thenReturn(Optional.of(student));

            // Act
            List<AttendanceStatsDTO> result = attendanceService.getAttendanceStatsByCourse(
                    teacherUser, course.getId(), classEntity.getId(), null, null);

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            AttendanceStatsDTO stats = result.get(0);
            assertEquals(classEntity.getId(), stats.getClassId());
            assertEquals(student.getId(), stats.getStudentId());
            assertEquals(student.getName(), stats.getStudentName());
            assertEquals(1, stats.getTotalClasses());
            assertEquals(1, stats.getPresentCount());
            assertEquals(100.0, stats.getAttendancePercentage());
        }
    }

 */
}