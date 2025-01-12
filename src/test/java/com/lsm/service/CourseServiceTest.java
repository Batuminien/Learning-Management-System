package com.lsm.service;

import com.lsm.exception.DuplicateResourceException;
import com.lsm.exception.ResourceNotFoundException;
import com.lsm.model.DTOs.CourseDTO;
import com.lsm.model.entity.Course;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.StudentDetails;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AppUserRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
/*
    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ClassEntityRepository classEntityRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private CourseService courseService;

    private Course testCourse;
    private CourseDTO testCourseDTO;
    private ClassEntity testClass;
    private AppUser testStudent;
    private AppUser testTeacher;
    private List<Long> classIds;

    @BeforeEach
    void setUp() {
        // Setup test class
        testClass = ClassEntity.builder()
                .id(1L)
                .name("Test Class")
                .build();

        // Setup test course
        testCourse = Course.builder()
                .id(1L)
                .name("Test Course")
                .code("TC101")
                .description("Test Description")
                .credits(3)
                .classes(List.of(testClass))
                .build();

        // Setup class IDs
        classIds = List.of(1L);

        // Setup course DTO
        testCourseDTO = CourseDTO.builder()
                .id(1L)
                .name("Test Course")
                .code("TC101")
                .description("Test Description")
                .credits(3)
                .classEntityIds(classIds)
                .build();

        // Setup test student
        testStudent = AppUser.builder()
                .id(1L)
                .role(Role.ROLE_STUDENT)
                .studentDetails(new StudentDetails())
                .build();
        testStudent.getStudentDetails().setClassEntity(1L);

        // Setup test teacher
        testTeacher = AppUser.builder()
                .id(2L)
                .role(Role.ROLE_TEACHER)
                .build();
    }

    @Test
    void getCourseById_WithValidId_ShouldReturnCourse() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // Act
        CourseDTO result = courseService.getCourseById(testTeacher, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testCourse.getId(), result.getId());
        assertEquals(testCourse.getName(), result.getName());
    }

    @Test
    void getCourseById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> courseService.getCourseById(testTeacher, 999L));
    }

    @Test
    void getAllCourses_ShouldReturnAllCourses() {
        // Arrange
        List<Course> courses = List.of(testCourse);
        when(courseRepository.findAll()).thenReturn(courses);

        // Act
        List<CourseDTO> result = courseService.getAllCourses();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCourse.getName(), result.get(0).getName());
    }

    @Test
    void createCourse_WithValidData_ShouldSucceed() {
        // Arrange
        when(courseRepository.existsByCode(testCourseDTO.getCode())).thenReturn(false);
        when(classEntityRepository.findById(any())).thenReturn(Optional.of(testClass));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // Act
        CourseDTO result = courseService.createCourse(testCourseDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testCourseDTO.getName(), result.getName());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_WithDuplicateCode_ShouldThrowException() {
        // Arrange
        when(courseRepository.existsByCode(testCourseDTO.getCode())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> courseService.createCourse(testCourseDTO));
    }

    @Test
    void updateCourse_WithValidData_ShouldSucceed() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.existsByCodeAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(classEntityRepository.findById(any())).thenReturn(Optional.of(testClass));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        testCourseDTO.setName("Updated Course");

        // Act
        CourseDTO result = courseService.updateCourse(1L, testCourseDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Course", result.getName());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void deleteCourse_WithValidId_ShouldSucceed() {
        // Arrange
        when(courseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(courseRepository).deleteById(1L);

        // Act
        courseService.deleteCourse(1L);

        // Assert
        verify(courseRepository).deleteById(1L);
    }

    @Test
    void getCoursesByClassId_WithValidId_ShouldReturnCourses() {
        // Arrange
        when(classEntityRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findByClassId(1L)).thenReturn(List.of(testCourse));

        // Act
        List<CourseDTO> result = courseService.getCoursesByClassId(1L);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCourse.getName(), result.get(0).getName());
    }

    @Test
    void getCoursesByStudent_WithValidId_ShouldReturnCourses() {
        // Arrange
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(classEntityRepository.findById(1L)).thenReturn(Optional.of(testClass));
        testClass.setCourses(Set.of(testCourse));

        // Act
        List<CourseDTO> result = courseService.getCoursesByStudent(1L);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCourse.getName(), result.get(0).getName());
    }

    @Test
    void getCoursesByTeacher_WithValidId_ShouldReturnCourses() {
        // Arrange
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(testTeacher));
        when(courseRepository.findByTeacherId(2L)).thenReturn(List.of(testCourse));

        // Act
        List<CourseDTO> result = courseService.getCoursesByTeacher(2L);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCourse.getName(), result.get(0).getName());
    }

    @Test
    void searchCourses_WithValidCriteria_ShouldReturnMatchingCourses() {
        // Arrange
        when(courseRepository.findAll(any(Specification.class))).thenReturn(List.of(testCourse));

        // Act
        List<CourseDTO> result = courseService.searchCourses("Test", "Spring", 2024);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCourse.getName(), result.get(0).getName());
    }

 */
}