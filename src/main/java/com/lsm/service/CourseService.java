package com.lsm.service;

import com.lsm.model.DTOs.ClassEntityResponseDTO;
import com.lsm.model.DTOs.CourseDTO;
import com.lsm.model.DTOs.StudentDTO;
import com.lsm.model.DTOs.TeacherCourseClassDTO;
import com.lsm.model.entity.Assignment;
import com.lsm.model.entity.Course;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.TeacherCourse;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AppUserRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.repository.CourseRepository;
import com.lsm.exception.DuplicateResourceException;
import com.lsm.exception.ResourceNotFoundException;

import com.lsm.repository.TeacherCourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final ClassEntityRepository classEntityRepository;
    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;
    private final TeacherCourseRepository teacherCourseRepository;

    @Cacheable(value = "courses", key = "#id", unless = "#result == null")
    public CourseDTO getCourseById(AppUser loggedInUser, Long id) {
        // TODO: constraints
        log.debug("Fetching course with id: {}", id);
        return courseRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> {
                    log.error("Course not found with id in getCourseById: {}", id);
                    return new ResourceNotFoundException("Course not found with id: " + id);
                });
    }

    @Cacheable(value = "courses", key = "'all'")
    public List<CourseDTO> getAllCourses() {
        log.debug("Fetching all courses");
        return courseRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "coursesByClass", key = "#classId")
    public List<CourseDTO> getCoursesByClassId(Long classId) {
        log.debug("Fetching courses for class id: {}", classId);

        // Verify class exists
        if (!classEntityRepository.existsById(classId)) {
            log.error("Class not found with id: {}", classId);
            throw new ResourceNotFoundException("Class not found with id: " + classId);
        }

        return courseRepository.findByClassId(classId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "coursesByStudent", key = "#studentId")
    public List<CourseDTO> getCoursesByStudent(Long studentId) {
        AppUser student = appUserRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        ClassEntity studentClass = student.getStudentDetails().getClassEntity();
        if (studentClass == null)
            throw new ResourceNotFoundException("Class not found in get courses by student");

        return studentClass.getTeacherCourses().stream()
                .map(tc -> mapToDTO(tc.getCourse()))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"courses", "coursesByClass", "coursesByStudent"}, allEntries = true)
    public CourseDTO createCourse(CourseDTO courseDTO) {
        log.debug("Creating new course with code: {}", courseDTO.getCode());

        validateCourseCode(courseDTO.getCode(), null);
        validateCourseData(courseDTO);

        Course course = buildCourseFromDTO(courseDTO);
        Course savedCourse = courseRepository.save(course);

        log.info("Created new course with id: {}", savedCourse.getId());
        return mapToDTO(savedCourse);
    }

    @CachePut(value = "courses", key = "#id")
    @CacheEvict(value = {"coursesByClass", "coursesByStudent"}, allEntries = true)
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        log.debug("Updating course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with id: {}", id);
                    return new ResourceNotFoundException("Course not found with id: " + id);
                });

        validateCourseCode(courseDTO.getCode(), id);
        validateCourseData(courseDTO);

        updateCourseFromDTO(course, courseDTO);
        Course updatedCourse = courseRepository.save(course);

        log.info("Updated course with id: {}", id);
        return mapToDTO(updatedCourse);
    }

    @CacheEvict(value = {"courses", "coursesByClass", "coursesByStudent"}, allEntries = true)
    public void deleteCourse(Long id) {
        log.debug("Deleting course with id: {}", id);

        if (!courseRepository.existsById(id)) {
            log.error("Course not found with id: {}", id);
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }

        courseRepository.deleteById(id);
        log.info("Deleted course with id: {}", id);
    }

    private void validateCourseCode(String code, Long excludeId) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty");
        }

        boolean exists = excludeId == null ?
                courseRepository.existsByCode(code) :
                courseRepository.existsByCodeAndIdNot(code, excludeId);

        if (exists) {
            log.error("Course with code {} already exists", code);
            throw new DuplicateResourceException("Course with code " + code + " already exists");
        }
    }

    private void validateCourseData(CourseDTO courseDTO) {
        if (courseDTO.getName() == null || courseDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty");
        }
        if (courseDTO.getCredits() == null || courseDTO.getCredits() <= 0) {
            throw new IllegalArgumentException("Course credits must be positive");
        }
    }

    private Course buildCourseFromDTO(CourseDTO courseDTO) {
        List<ClassEntity> classes = courseDTO.getClassEntityIds() != null ?
                courseDTO.getClassEntityIds().stream()
                        .map(id -> classEntityRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id)))
                        .collect(Collectors.toList()) :
                List.of();

        return Course.builder()
                .name(courseDTO.getName())
                .description(courseDTO.getDescription())
                .code(courseDTO.getCode())
                .credits(courseDTO.getCredits())
                .classes(classes)
                .build();
    }

    private void updateCourseFromDTO(Course course, CourseDTO courseDTO) {
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setCode(courseDTO.getCode());
        course.setCredits(courseDTO.getCredits());

        if (courseDTO.getClassEntityIds() != null) {
            List<ClassEntity> classes = courseDTO.getClassEntityIds().stream()
                    .map(id -> classEntityRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id)))
                    .collect(Collectors.toList());
            course.setClasses(classes);
        }
    }

    @Cacheable(value = "coursesByTeacher", key = "#teacherId")
    public List<CourseDTO> getCoursesByTeacher(Long teacherId) {
        AppUser teacher = appUserService.getCurrentUserWithDetails(teacherId);

        if (teacher.getRole() != Role.ROLE_TEACHER) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        return teacher.getTeacherDetails().getTeacherCourses().stream()
                .map(tc -> mapToDTO(tc.getCourse()))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "courses", key = "'search:' + #query + ':' + #semester + ':' + #year")
    public List<CourseDTO> searchCourses(String query, String semester, Integer year) {
        log.debug("Searching courses with query: {}, semester: {}, year: {}", query, semester, year);

        // Create specification for dynamic filtering
        Specification<Course> spec = Specification.where(null);

        if (query != null && !query.trim().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + query.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), "%" + query.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + query.toLowerCase() + "%")
                    )
            );
        }

        if (semester != null && !semester.trim().isEmpty()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("semester"), semester)
            );
        }

        if (year != null) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("year"), year)
            );
        }

        return courseRepository.findAll(spec).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"coursesByTeacher", "courses"}, allEntries = true)
    public CourseDTO assignTeacherToCourse(Long courseId, Long teacherId, List<Long> classIds) {
        log.debug("Assigning teacher {} to course {} with classes {}", teacherId, courseId, classIds);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        AppUser teacher = appUserRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        if (teacher.getRole() != Role.ROLE_TEACHER) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        // Remove existing teacher-course relationship if it exists
        course.getTeacherCourses().removeIf(tc -> tc.getTeacher().getId().equals(teacherId));

        // Create and properly initialize the TeacherCourse entity
        Set<ClassEntity> classes = new HashSet<>(classEntityRepository.findAllByIdIn(classIds));
        if (classes.size() != classIds.size()) {
            throw new ResourceNotFoundException("One or more classes not found");
        }

        TeacherCourse teacherCourse = TeacherCourse.builder()
                .teacher(teacher)
                .course(course)
                .classes(classes)
                .build();

        // Set up bidirectional relationship
        course.getTeacherCourses().add(teacherCourse);
        teacher.getTeacherDetails().getTeacherCourses().add(teacherCourse);

        // First save TeacherCourse
        teacherCourseRepository.save(teacherCourse);  // Add this line

        // Then save the course
        Course savedCourse = courseRepository.save(course);
        log.info("Successfully assigned teacher {} to course {}", teacherId, courseId);

        return mapToDTO(savedCourse);
    }

    @CacheEvict(value = {"coursesByTeacher", "courses"}, allEntries = true)
    public CourseDTO removeTeacherFromCourse(Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        TeacherCourse teacherCourseToRemove = course.getTeacherCourses().stream()
                .filter(tc -> tc.getTeacher().getId().equals(teacherId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not assigned to this course"));

        // Remove from both sides
        course.getTeacherCourses().remove(teacherCourseToRemove);
        AppUser teacher = teacherCourseToRemove.getTeacher();
        if (teacher != null && teacher.getTeacherDetails() != null) {
            teacher.getTeacherDetails().getTeacherCourses().remove(teacherCourseToRemove);
        }

        return mapToDTO(courseRepository.save(course));
    }

    @CacheEvict(value = {"coursesByTeacher", "courses"}, allEntries = true)
    public CourseDTO updateCourseTeacher(Long courseId, Long newTeacherId, List<Long> classIds) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        AppUser newTeacher = appUserRepository.findById(newTeacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + newTeacherId));

        if (newTeacher.getRole() != Role.ROLE_TEACHER) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        // Remove old teacher course relationships properly
        Set<TeacherCourse> oldTeacherCourses = new HashSet<>(course.getTeacherCourses());
        for (TeacherCourse tc : oldTeacherCourses) {
            AppUser oldTeacher = tc.getTeacher();
            if (oldTeacher != null && oldTeacher.getTeacherDetails() != null) {
                oldTeacher.getTeacherDetails().getTeacherCourses().remove(tc);
            }
        }
        course.getTeacherCourses().clear();

        Set<ClassEntity> classes = new HashSet<>(classEntityRepository.findAllByIdIn(classIds));
        if (classes.size() != classIds.size()) {
            throw new ResourceNotFoundException("One or more classes not found");
        }

        TeacherCourse teacherCourse = TeacherCourse.builder()
                .teacher(newTeacher)
                .course(course)
                .classes(classes)
                .build();

        course.getTeacherCourses().add(teacherCourse);
        newTeacher.getTeacherDetails().getTeacherCourses().add(teacherCourse);

        return mapToDTO(courseRepository.save(course));
    }

    public List<ClassEntityResponseDTO> getTeacherCourseClasses(Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        AppUser teacher = appUserService.getCurrentUserWithDetails(teacherId);

        return course.getTeacherCourses().stream()
                .filter(tc -> tc.getTeacher().getId().equals(teacherId))
                .flatMap(tc -> tc.getClasses().stream())
                .map(this::mapToClassEntityResponseDTO)
                .collect(Collectors.toList());
    }

    private ClassEntityResponseDTO mapToClassEntityResponseDTO(ClassEntity classEntity) {
        Map<Long, String> studentIdAndNames = classEntity.getStudents().stream()
                .collect(Collectors.toMap(
                        AppUser::getId,
                        student -> student.getName() + " " + student.getSurname()
                ));

        return ClassEntityResponseDTO.builder()
                .id(classEntity.getId())
                .name(classEntity.getName())
                .description(classEntity.getDescription())
                .studentIdAndNames(studentIdAndNames)
                .assignmentIds(classEntity.getAssignments().stream()
                        .map(Assignment::getId)
                        .collect(Collectors.toList()))
                .build();
    }

    public CourseDTO updateTeacherCourseClasses(Long courseId, Long teacherId, List<Long> classIds) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        AppUser teacher = appUserRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

        if (teacher.getRole() != Role.ROLE_TEACHER) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        Set<ClassEntity> classes = new HashSet<>(classEntityRepository.findAllByIdIn(classIds));
        if (classes.size() != classIds.size()) {
            throw new ResourceNotFoundException("One or more classes not found");
        }

        // Find and update existing TeacherCourse
        TeacherCourse teacherCourse = course.getTeacherCourses().stream()
                .filter(tc -> tc.getTeacher().getId().equals(teacherId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Teacher is not assigned to this course"));

        teacherCourse.setClasses(classes);
        return mapToDTO(courseRepository.save(course));
    }

    private StudentDTO mapToStudentDTO(AppUser user) {
        if (user.getRole() != Role.ROLE_STUDENT || user.getStudentDetails() == null) {
            throw new IllegalArgumentException("User is not a student");
        }

        return StudentDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .phone(user.getStudentDetails().getPhone())
                .tc(user.getStudentDetails().getTc())
                .birthDate(user.getStudentDetails().getBirthDate())
                .registrationDate(user.getStudentDetails().getRegistrationDate())
                .parentName(user.getStudentDetails().getParentName())
                .parentPhone(user.getStudentDetails().getParentPhone())
                .classEntityId(user.getStudentDetails().getClassEntity().getId())
                .build();
    }

    private CourseDTO mapToDTO(Course course) {
        List<TeacherCourseClassDTO> teacherCourses = course.getTeacherCourses().stream()
                .map(tc -> TeacherCourseClassDTO.builder()
                        .teacherId(tc.getTeacher().getId())
                        .teacherName(tc.getTeacher().getName())
                        .courseId(tc.getCourse().getId())
                        .classIds(tc.getClasses().stream()
                                .map(ClassEntity::getId)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return CourseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .code(course.getCode())
                .credits(course.getCredits())
                .classEntityIds(course.getClasses().stream()
                        .map(ClassEntity::getId)
                        .collect(Collectors.toList()))
                .teacherCourses(teacherCourses)
                .build();
    }
}