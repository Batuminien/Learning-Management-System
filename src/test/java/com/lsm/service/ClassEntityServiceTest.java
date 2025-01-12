package com.lsm.service;

import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.TeacherDetails;
import com.lsm.model.entity.StudentDetails;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AppUserRepository;
import com.lsm.repository.ClassEntityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.nio.file.AccessDeniedException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassEntityServiceTest {
/*
    @Mock
    private ClassEntityRepository classRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ClassEntityService classService;

    private AppUser teacherUser;
    private AppUser studentUser;
    private ClassEntity testClass;
    private List<Long> studentIds;

    @BeforeEach
    void setUp() {
        // Set up teacher
        teacherUser = AppUser.builder()
                .id(1L)
                .username("teacher")
                .role(Role.ROLE_TEACHER)
                .teacherDetails(new TeacherDetails())
                .build();
        teacherUser.getTeacherDetails().setClasses(new HashSet<>());

        // Set up student
        studentUser = AppUser.builder()
                .id(2L)
                .username("student")
                .role(Role.ROLE_STUDENT)
                .studentDetails(new StudentDetails())
                .build();

        // Set up class
        testClass = ClassEntity.builder()
                .id(1L)
                .name("Test Class")
                .description("Test Description")
                .teacher(teacherUser)
                .students(new HashSet<>())
                .build();

        studentIds = Collections.singletonList(2L);
    }

    @Test
    void createClass_WithValidData_ShouldSucceed() throws AccessDeniedException {
        // Arrange
        when(appUserRepository.findById(teacherUser.getId())).thenReturn(Optional.of(teacherUser));
        when(appUserRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(classRepository.save(any(ClassEntity.class))).thenReturn(testClass);

        // Act
        ClassEntity result = classService.createClass(teacherUser, testClass, teacherUser.getId(), studentIds);

        // Assert
        assertNotNull(result);
        assertEquals(testClass.getName(), result.getName());
        verify(classRepository).save(any(ClassEntity.class));
    }

    @Test
    void createClass_WithInvalidTeacher_ShouldThrowException() {
        // Arrange
        when(appUserRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> classService.createClass(teacherUser, testClass, 999L, studentIds));
    }

    @Test
    void getClassById_WithValidId_ShouldReturnClass() throws AccessDeniedException {
        // Arrange
        teacherUser.getTeacherDetails().getClasses().add(testClass);
        when(classRepository.findById(testClass.getId())).thenReturn(Optional.of(testClass));

        // Act
        ClassEntity result = classService.getClassById(teacherUser, testClass.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testClass.getId(), result.getId());
    }

    @Test
    void getClassById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(classRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> classService.getClassById(teacherUser, 999L));
    }

    @Test
    void getAllClasses_WhenAuthenticated_ShouldReturnAllClasses() {
        // Arrange
        List<ClassEntity> classes = Arrays.asList(testClass);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(classRepository.findAllWithAssociations()).thenReturn(classes);

        // Act
        List<ClassEntity> result = classService.getAllClasses(authentication);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(classes.size(), result.size());
    }

    @Test
    void updateClass_WithValidData_ShouldSucceed() throws AccessDeniedException {
        // Arrange
        ClassEntity updatedClass = testClass;
        updatedClass.setName("Updated Name");

        when(classRepository.findById(testClass.getId())).thenReturn(Optional.of(testClass));
        when(classRepository.save(any(ClassEntity.class))).thenReturn(updatedClass);
        when(appUserRepository.findById(teacherUser.getId())).thenReturn(Optional.of(teacherUser)); // Add this line
        teacherUser.getTeacherDetails().getClasses().add(testClass);

        // Act
        ClassEntity result = classService.updateClass(teacherUser, testClass.getId(), updatedClass,
                teacherUser.getId(), null);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void addStudent_WhenStudentNotAlreadyInClass_ShouldAddSuccessfully() throws AccessDeniedException {
        // Arrange
        teacherUser.getTeacherDetails().getClasses().add(testClass);
        when(classRepository.findById(testClass.getId())).thenReturn(Optional.of(testClass));
        when(appUserRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(classRepository.save(any(ClassEntity.class))).thenReturn(testClass);

        // Act
        ClassEntity result = classService.addStudent(teacherUser, testClass.getId(), studentUser.getId());

        // Assert
        assertNotNull(result);
        verify(classRepository).save(any(ClassEntity.class));
    }

    @Test
    void removeStudent_WhenStudentExists_ShouldRemoveSuccessfully() throws AccessDeniedException {
        // Arrange
        testClass.getStudents().add(studentUser);
        teacherUser.getTeacherDetails().getClasses().add(testClass);

        when(classRepository.findById(testClass.getId())).thenReturn(Optional.of(testClass));
        when(appUserRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(classRepository.save(any(ClassEntity.class))).thenReturn(testClass);

        // Act
        ClassEntity result = classService.removeStudent(teacherUser, testClass.getId(), studentUser.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.getStudents().isEmpty());
    }

    @Test
    void getTeacherClasses_ShouldReturnTeacherClasses() throws AccessDeniedException {
        // Arrange
        List<ClassEntity> teacherClasses = Arrays.asList(testClass);
        when(authentication.getPrincipal()).thenReturn(teacherUser);
        when(classRepository.findClassesByTeacherId(teacherUser.getId())).thenReturn(teacherClasses);

        // Act
        List<ClassEntity> result = classService.getTeacherClasses(authentication);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(teacherClasses.size(), result.size());
    }

    @Test
    void getStudentClasses_ShouldReturnStudentClass() throws AccessDeniedException {
        // Arrange
        studentUser.getStudentDetails().setClassEntity(testClass.getId());
        when(authentication.getPrincipal()).thenReturn(studentUser);
        when(classRepository.getClassEntityById(testClass.getId())).thenReturn(Optional.of(testClass));

        // Act
        ClassEntity result = classService.getStudentClasses(authentication);

        // Assert
        assertNotNull(result);
        assertEquals(testClass.getId(), result.getId());
    }

 */
}