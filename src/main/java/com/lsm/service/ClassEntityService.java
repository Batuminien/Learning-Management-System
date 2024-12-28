package com.lsm.service;

import com.lsm.model.DTOs.TeacherCourseDTO;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.Course;
import com.lsm.model.entity.TeacherCourse;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AppUserRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.nio.file.AccessDeniedException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClassEntityService {

    private final ClassEntityRepository classRepository;
    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;
    private final CourseRepository courseRepository;

    @Autowired
    public ClassEntityService(ClassEntityRepository classRepository, AppUserRepository appUserRepository, AppUserService appUserService, CourseRepository courseRepository) {
        this.classRepository = classRepository;
        this.appUserRepository = appUserRepository;
        this.appUserService = appUserService;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public ClassEntity createClass(AppUser loggedInUser, ClassEntity classEntity,
                                   List<TeacherCourseDTO> teacherCourses, List<Long> studentIds) {

        Set<TeacherCourse> teacherCourseSet = createTeacherCourses(teacherCourses, classEntity);
        Set<AppUser> students = getStudents(studentIds, classEntity);

        classEntity.setTeacherCourses(teacherCourseSet);
        classEntity.setStudents(students);

        return classRepository.save(classEntity);
    }

    @Transactional
    public ClassEntity getClassById(AppUser loggedInUser, Long id) throws AccessDeniedException, EntityNotFoundException {
        ClassEntity classEntity = classRepository.findByIdWithAssignments(id)
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + id));

        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());

        if (user.getRole().equals(Role.ROLE_STUDENT)
                && !user.getStudentDetails().getClassEntity().equals(classEntity.getId()))
            throw new AccessDeniedException("Students can't get the class which they are not enrolled.");

        if (user.getRole().equals(Role.ROLE_TEACHER)
                && user.getTeacherDetails().getTeacherCourses().stream()
                .noneMatch(tc -> tc.getClasses().stream()
                        .anyMatch(c -> c.getId().equals(classEntity.getId()))))
            throw new AccessDeniedException("Students can't get the class which they are not teaching.");

        return classEntity;
    }

    @Transactional(readOnly = true)
    public List<ClassEntity> getAllClasses(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        return classRepository.findAllWithAssociations();
    }

    @Transactional
    public ClassEntity updateClass(AppUser loggedInUser, Long id, ClassEntity classEntity,
                                   List<TeacherCourseDTO> teacherCourses, List<Long> studentIds)
            throws AccessDeniedException {

        ClassEntity existingClass = getClassById(loggedInUser, id);
        existingClass.setName(classEntity.getName());
        existingClass.setDescription(classEntity.getDescription());

        if (teacherCourses != null) {
            Set<TeacherCourse> newTeacherCourses = createTeacherCourses(teacherCourses, existingClass);
            existingClass.setTeacherCourses(newTeacherCourses);
        }

        if (studentIds != null) {
            Set<AppUser> students = getStudents(studentIds, existingClass);
            existingClass.setStudents(students);
        }

        return classRepository.save(existingClass);
    }

    private Set<TeacherCourse> createTeacherCourses(List<TeacherCourseDTO> teacherCourses, ClassEntity classEntity) {
        return teacherCourses.stream()
                .map(tc -> {
                    AppUser teacher = appUserRepository.findById(tc.getTeacherId())
                            .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
                    Course course = courseRepository.findById(tc.getCourseId())
                            .orElseThrow(() -> new EntityNotFoundException("Course not found"));

                    return TeacherCourse.builder()
                            .teacher(teacher)
                            .course(course)
                            .classes(Set.of(classEntity))
                            .build();
                })
                .collect(Collectors.toSet());
    }

    private Set<AppUser> getStudents(List<Long> studentIds, ClassEntity classEntity) {
        return studentIds.stream()
                .map(id -> appUserRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Student not found")))
                .peek(s -> s.getStudentDetails().setClassEntity(classEntity.getId()))
                .collect(Collectors.toSet());
    }

    @Transactional
    public void deleteClass(Long id) throws AccessDeniedException, EntityNotFoundException {
        if (!classRepository.existsById(id)) {
            throw new EntityNotFoundException("Class not found with id: " + id);
        }
        classRepository.deleteById(id);
    }

    @Transactional
    public ClassEntity addStudent(AppUser loggedInUser, Long classId, Long studentId)
            throws AccessDeniedException, EntityNotFoundException {
        ClassEntity classEntity = getClassById(loggedInUser, classId);
        AppUser student = appUserRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        if (classEntity.getStudents() == null) {
            classEntity.setStudents(new HashSet<>());
        }

        // Check if student is already in the class
        if (classEntity.getStudents().stream().noneMatch(s -> s.getId().equals(studentId))) {
            classEntity.getStudents().add(student);
            classRepository.save(classEntity);
        }
        return classEntity;
    }

    @Transactional
    public ClassEntity addStudentsBulk(AppUser loggedInUser, Long classId, List<Long> studentIds) throws AccessDeniedException {
        ClassEntity classEntity = getClassById(loggedInUser, classId);

        // Check if the current user has permission to modify this class
        /*
        if (!hasPermissionToModify(classEntity)) {
            throw new AccessDeniedException("You don't have permission to modify this class");
        }
         */

        // Validate all student IDs exist before adding any
        List<AppUser> studentsToAdd = appUserRepository.findAllById(studentIds);
        if (studentsToAdd.size() != studentIds.size()) {
            throw new EntityNotFoundException("One or more student IDs are invalid");
        }

        // Add all students to the class
        classEntity.getStudents().addAll(studentsToAdd);

        // Save and return the updated class
        return classRepository.save(classEntity);
    }

    @Transactional
    public ClassEntity removeStudent(AppUser loggedInUser, Long classId, Long studentId)
            throws AccessDeniedException, EntityNotFoundException {
        ClassEntity classEntity = getClassById(loggedInUser, classId);

        // Check if student exists before attempting to remove
        appUserRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        boolean removed = classEntity.getStudents().removeIf(student -> student.getId().equals(studentId));
        if (!removed) {
            throw new EntityNotFoundException("Student with id " + studentId + " not found in class " + classId);
        }

        classRepository.save(classEntity);
        return classEntity;
    }

    @Transactional(readOnly = true)
    public List<ClassEntity> getTeacherClasses(Authentication authentication) throws AccessDeniedException {
        AppUser teacher = (AppUser) authentication.getPrincipal();
        return classRepository.findClassesByTeacherId(teacher.getId());
    }

    @Transactional(readOnly = true)
    public ClassEntity getStudentClasses(Authentication authentication) throws AccessDeniedException {
        AppUser student = (AppUser) authentication.getPrincipal();
        return classRepository.findByIdWithAssignments(student.getStudentDetails().getClassEntity())
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " +
                        student.getStudentDetails().getClassEntity()));
    }
}