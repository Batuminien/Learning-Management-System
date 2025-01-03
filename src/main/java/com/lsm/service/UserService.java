package com.lsm.service;

import com.lsm.exception.DuplicateResourceException;
import com.lsm.exception.IllegalOperationException;
import com.lsm.exception.ResourceNotFoundException;
import com.lsm.model.DTOs.*;
import com.lsm.model.entity.*;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AppUserRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.repository.CourseRepository;
import com.lsm.repository.ProfilePhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final AppUserRepository userRepository;
    private final ClassEntityRepository classRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfilePhotoRepository profilePhotoRepository;

    // Generic user operations
    public Page<AppUser> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<AppUser> getAllStudents(Pageable pageable) {
        return userRepository.findAllByRole(Role.ROLE_STUDENT, pageable);
    }

    public Page<AppUser> getAllTeachers(Pageable pageable) {
        return userRepository.findAllByRole(Role.ROLE_TEACHER, pageable);
    }

    public Page<AppUser> getAllCoordinators(Pageable pageable) {
        return userRepository.findAllByRole(Role.ROLE_COORDINATOR, pageable);
    }

    public AppUser getUserById(Long id) {
        return userRepository.findUserWithAllDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public void deleteUser(Long id) {
        AppUser user = getUserById(id);
        userRepository.delete(user);
    }

    @Transactional
    public AppUser updateUser(Long id, UserUpdateRequestDTO updateRequest) {
        AppUser user = getUserById(id);

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
            validateEmailUnique(updateRequest.getEmail(), id);
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getFirstName() != null && !updateRequest.getFirstName().isBlank()) {
            user.setName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null && !updateRequest.getLastName().isBlank()) {
            user.setSurname(updateRequest.getLastName());
        }
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public AppUser updateStudent(Long id, StudentUpdateRequestDTO updateRequest) {
        AppUser student = getUserById(id);
        if (student.getRole() != Role.ROLE_STUDENT) {
            throw new IllegalOperationException("User with id: " + id + " is not a student");
        }

        // Update basic user information first
        updateUser(id, updateRequest);

        // Update student-specific details
        StudentDetails details = student.getStudentDetails();
        if (details == null) {
            details = new StudentDetails();
            student.setStudentDetails(details);
        }

        if (updateRequest.getPhone() != null && !updateRequest.getPhone().isBlank()) {
            details.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getTc() != null && !updateRequest.getTc().isBlank()) {
            details.setTc(updateRequest.getTc());
        }
        if (updateRequest.getBirthDate() != null) {
            details.setBirthDate(updateRequest.getBirthDate());
        }
        if (updateRequest.getRegistrationDate() != null) {
            details.setRegistrationDate(updateRequest.getRegistrationDate());
        }
        if (updateRequest.getParentName() != null && !updateRequest.getParentName().isBlank()) {
            details.setParentName(updateRequest.getParentName());
        }
        if (updateRequest.getParentPhone() != null && !updateRequest.getParentPhone().isBlank()) {
            details.setParentPhone(updateRequest.getParentPhone());
        }
        if (updateRequest.getClassId() != null) {
            // Verify that the class exists
            ClassEntity classEntity = classRepository.findById(updateRequest.getClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + updateRequest.getClassId()));
            details.setClassEntity(classEntity);
        }

        return userRepository.save(student);
    }

    @Transactional
    public AppUser updateTeacher(Long id, TeacherUpdateRequestDTO updateRequest) {
        AppUser teacher = getUserById(id);
        if (teacher.getRole() != Role.ROLE_TEACHER) {
            throw new IllegalOperationException("User with id: " + id + " is not a teacher");
        }

        updateUser(id, updateRequest);

        TeacherDetails details = teacher.getTeacherDetails();
        if (details == null) {
            details = new TeacherDetails();
            teacher.setTeacherDetails(details);
        }

        if (updateRequest.getPhone() != null && !updateRequest.getPhone().isBlank()) {
            details.setPhone(updateRequest.getPhone());
        }

        if (updateRequest.getTc() != null && !updateRequest.getTc().isBlank()) {
            details.setTc(updateRequest.getTc());
        }

        if (updateRequest.getBirthDate() != null) {
            details.setBirthDate(updateRequest.getBirthDate());
        }

        if (updateRequest.getTeacherCourses() != null && !updateRequest.getTeacherCourses().isEmpty()) {
            Set<TeacherCourse> teacherCourses = new HashSet<>();

            for (TeacherCourseClassDTO courseDTO : updateRequest.getTeacherCourses()) {
                Course course = courseRepository.findById(courseDTO.getCourseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseDTO.getCourseId()));

                Set<ClassEntity> classes = new HashSet<>(classRepository.findAllByIdIn(courseDTO.getClassIds()));
                if (classes.size() != courseDTO.getClassIds().size()) {
                    throw new ResourceNotFoundException("One or more classes not found");
                }

                TeacherCourse teacherCourse = TeacherCourse.builder()
                        .course(course)
                        .classes(classes)
                        .teacher(teacher)
                        .build();

                teacherCourses.add(teacherCourse);
            }

            details.setTeacherCourses(teacherCourses);
        }

        return userRepository.save(teacher);
    }

    @Transactional
    public void deleteCoordinator(Long id) {
        AppUser coordinator = getUserById(id);
        if (coordinator.getRole() != Role.ROLE_COORDINATOR) {
            throw new IllegalOperationException("User with id: " + id + " is not a coordinator");
        }

        TeacherDetails details = coordinator.getTeacherDetails();
        if (details != null && details.getTeacherCourses() != null) {
            details.getTeacherCourses().clear();
        }

        userRepository.delete(coordinator);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        AppUser teacher = getUserById(id);
        if (teacher.getRole() != Role.ROLE_TEACHER) {
            throw new IllegalOperationException("User with id: " + id + " is not a teacher");
        }

        TeacherDetails details = teacher.getTeacherDetails();
        if (details != null && details.getTeacherCourses() != null) {
            details.getTeacherCourses().clear();
        }

        userRepository.delete(teacher);
    }

    @Transactional
    public void deleteStudent(Long id) {
        AppUser student = getUserById(id);
        if (student.getRole() != Role.ROLE_STUDENT) {
            throw new IllegalOperationException("User with id: " + id + " is not a student");
        }

        // Remove student from any associated classes
        if (student.getStudentDetails() != null && student.getStudentDetails().getClassEntity() != null) {
            ClassEntity classEntity = student.getStudentDetails().getClassEntity();
            classEntity.getStudents().remove(student);
            classRepository.save(classEntity);
        }

        userRepository.delete(student);
    }

    @Transactional
    public AppUser updateUserProfilePhoto(Long userId, ProfilePhotoUpdateRequestDTO photoUpdate) {
        AppUser user = getUserById(userId);

        // Delete existing photo if it exists
        profilePhotoRepository.findByUser(user).ifPresent(profilePhotoRepository::delete);

        // Create new photo entity
        ProfilePhoto newPhoto = ProfilePhoto.builder()
                .user(user)
                .photoUrl(photoUpdate.getPhotoUrl())
                .filename(photoUpdate.getFilename())
                .fileType(photoUpdate.getFileType())
                .fileSize(photoUpdate.getFileSize())
                .uploadTime(LocalDateTime.now())
                .build();

        // Save new photo and update user
        profilePhotoRepository.save(newPhoto);
        user.setProfilePhoto(newPhoto);
        return userRepository.save(user);
    }

    @Transactional
    public AppUser removeUserProfilePhoto(Long userId) {
        AppUser user = getUserById(userId);

        // Find and delete the profile photo
        ProfilePhoto photo = profilePhotoRepository.findByUser(user)
                .orElse(null);

        if (photo != null) {
            profilePhotoRepository.delete(photo);
            user.setProfilePhoto(null);
            return userRepository.save(user);
        }

        return user; // Return unchanged user if no photo exists
    }

    // Helper methods
    private void validateEmailUnique(String email, Long excludeUserId) {
        Optional<AppUser> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(excludeUserId)) {
            throw new DuplicateResourceException("Email already exists: " + email);
        }
    }
}