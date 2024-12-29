package com.lsm.service;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.lsm.model.DTOs.*;
import com.lsm.model.entity.*;
import com.lsm.model.entity.enums.AssignmentStatus;
import com.lsm.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;

import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AppUserRepository appUserRepository;
    private final ClassEntityRepository classEntityRepository;
    private final AssignmentDocumentRepository assignmentDocumentRepository;
    private final StudentSubmissionRepository studentSubmissionRepository;
    private final CourseRepository courseRepository;
    private final AppUserService appUserService;
    private final AssignmentDocumentService assignmentDocumentService;
    private final StudentSubmissionService studentSubmissionService;
    private final TeacherCourseRepository teacherCourseRepository;

    @Value("${assignment.max-title-length:100}")
    private int maxTitleLength;

    @Value("${assignment.min-due-date-days:1}")
    private int minDueDateDays;

    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepository, AppUserRepository appUserRepository,
                             ClassEntityRepository classEntityRepository, AssignmentDocumentRepository assignmentDocumentRepository,
                             StudentSubmissionRepository studentSubmissionRepository, CourseRepository courseRepository,
                             AppUserService appUserService, AssignmentDocumentService assignmentDocumentService,
                             StudentSubmissionService studentSubmissionService, TeacherCourseRepository teacherCourseRepository) {
        this.assignmentRepository = assignmentRepository;
        this.appUserRepository = appUserRepository;
        this.classEntityRepository = classEntityRepository;
        this.assignmentDocumentRepository = assignmentDocumentRepository;
        this.studentSubmissionRepository = studentSubmissionRepository;
        this.courseRepository = courseRepository;
        this.appUserService = appUserService;
        this.assignmentDocumentService = assignmentDocumentService;
        this.studentSubmissionService = studentSubmissionService;
        this.teacherCourseRepository = teacherCourseRepository;
    }

    @Transactional
    public List<AssignmentDTO> getAllAssignments(AppUser currentUser) throws AccessDeniedException {
        if (currentUser.getRole() == Role.ROLE_STUDENT || currentUser.getRole() == Role.ROLE_TEACHER)
            throw new AccessDeniedException("Only admin and coordinator can list all assignments");
        List<Assignment> assignments = assignmentRepository.findAll();
        return assignments.stream()
                .map(assignment -> new AssignmentDTO(assignment, "Retrieved successfully"))
                .collect(Collectors.toList());
    }

    @Transactional
    public Assignment createAssignment(AssignmentRequestDTO dto, Long loggedInUserId)
            throws AccessDeniedException {
        try {
            log.info("Creating assignment with title: {}", dto.getTitle());

            validateAssignmentRequest(dto);

            AppUser teacher = appUserRepository.findById(loggedInUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

            if (teacher.getRole().equals(Role.ROLE_STUDENT))
                throw new AccessDeniedException("Students can't create assignments");

            ClassEntity classEntity = classEntityRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found"));

            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException("Course not found"));

            TeacherCourse teacherCourse = teacherCourseRepository.findByTeacherAndCourse(teacher, course)
                    .orElseThrow(() -> new EntityNotFoundException("Teacher is not assigned to this course"));

            validateTeacherAccess(teacher, classEntity);

            Assignment assignment = createAssignmentEntity(dto, teacher, classEntity, course, teacherCourse); // Pass teacherCourse

            log.info("Assignment created successfully with ID: {}", assignment.getId());
            return assignmentRepository.save(assignment);

        } catch (Exception e) {
            log.error("Error creating assignment: {}", e.getMessage());
            throw e;
        }
    }


    @Transactional
    public List<AssignmentDTO> getAssignmentsByTeacher(Long teacherId,
                                                       Long classId, Long courseId, LocalDate dueDate,
                                                       AppUser loggedInUser)
            throws AccessDeniedException, EntityNotFoundException {
        // Find teacher
        AppUser teacher = appUserRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        // Validate that user is actually a teacher
        if (teacher.getRole().equals(Role.ROLE_STUDENT)) {
            throw new IllegalArgumentException("Specified user is not a teacher");
        }

        if (teacher.getRole().equals(Role.ROLE_TEACHER) && !loggedInUser.getId().equals(teacher.getId()))
            throw new AccessDeniedException("Mismatch between logged in user id and the teacher id.");

        // Get all assignments created by the teacher
        List<Assignment> assignments = assignmentRepository.findByAssignedByOrderByDueDateDesc(teacher);

        // Apply filters based on AssignmentFilterDTO
        if (classId != null) {
            assignments = assignments.stream()
                    .filter(assignment -> assignment.getClassEntity().getId().equals(classId))
                    .collect(Collectors.toList());
        }
        if (courseId != null) {
            assignments = assignments.stream()
                    .filter(assignment -> assignment.getCourse().getId().equals(courseId))
                    .collect(Collectors.toList());
        }
        if (dueDate != null) {
            assignments = assignments.stream()
                    .filter(assignment -> !assignment.getDueDate().isAfter(dueDate))
                    .collect(Collectors.toList());
        }

        return assignments.stream()
                .map(assignment -> new AssignmentDTO(assignment, "Retrieved successfully"))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<StudentAssignmentViewDTO> getAssignmentsByStudent(Long studentId)
            throws AccessDeniedException, EntityNotFoundException {
        // Find student
        AppUser student = appUserRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        // Validate that user is actually a student
        if (student.getRole() != Role.ROLE_STUDENT) {
            throw new IllegalArgumentException("Specified user is not a student");
        }

        // Add null checks for student details and class
        if (student.getStudentDetails() == null) {
            throw new EntityNotFoundException("Student details not found for student: " + studentId);
        }

        ClassEntity classEntity = student.getStudentDetails().getClassEntity();
        if (classEntity == null) {
            throw new EntityNotFoundException("Class not assigned for student: " + studentId);
        }

        // Get all assignments for the student's class
        Set<Assignment> assignments = assignmentRepository.findByClassEntityOrderByDueDateDesc(classEntity);

        // Convert to StudentAssignmentViewDTO
        return assignments.stream()
                .map(assignment -> new StudentAssignmentViewDTO(assignment, studentId))
                .collect(Collectors.toList());
    }

    @Transactional
    public Assignment updateAssignment(Long assignmentId, AssignmentRequestDTO dto, Long loggedInUserId)
            throws AccessDeniedException {
        AppUser user = appUserRepository.findById(loggedInUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if(user.getRole() == Role.ROLE_STUDENT) {
            throw new AccessDeniedException("Students can't update assignments");
        }
        try {
            log.info("Updating assignment ID: {} with title: {}", assignmentId, dto.getTitle());

            Assignment existingAssignment = assignmentRepository.findByIdWithSubmissions(assignmentId)
                    .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

            ClassEntity classEntity = classEntityRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found"));

            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException("Course not found"));

            // Validate teacher access
            validateTeacherAccess(existingAssignment.getAssignedBy(), classEntity);

            // Update assignment fields
            updateAssignmentFields(existingAssignment, dto, classEntity, course);
            existingAssignment.setLastModified(LocalDate.now());
            existingAssignment.setLastModifiedBy(user);

            log.info("Assignment updated successfully: {}", assignmentId);
            return assignmentRepository.save(existingAssignment);

        } catch (Exception e) {
            log.error("Error updating assignment {}: {}", assignmentId, e.getMessage());
            throw e;
        }
    }

    private void updateAssignmentFields(Assignment assignment, AssignmentRequestDTO dto,
                                        ClassEntity classEntity, Course course) {
        assignment.setTitle(dto.getTitle().trim());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setClassEntity(classEntity);
        assignment.setCourse(course);
    }

    @Transactional
    public void deleteAssignment(Long assignmentId, AppUser loggedInUser)
            throws AccessDeniedException {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());

        // Authorization check
        if (user.getRole() != Role.ROLE_ADMIN &&
                loggedInUser.getRole() != Role.ROLE_COORDINATOR &&
                !assignment.getAssignedBy().getId().equals(loggedInUser.getId())) {
            throw new AccessDeniedException("You can only delete your own assignments");
        }

        // Delete physical files if they exist
        if (assignment.getTeacherDocument() != null) {
            try {
                Files.deleteIfExists(Paths.get(assignment.getTeacherDocument().getFilePath()));
            } catch (IOException e) {
                log.error("Could not delete teacher document file: {}", e.getMessage());
            }
        }

        // Delete student submission files
        for (StudentSubmission submission : assignment.getStudentSubmissions()) {
            if (submission.getDocument() != null) {
                try {
                    Files.deleteIfExists(Paths.get(submission.getDocument().getFilePath()));
                } catch (IOException e) {
                    log.error("Could not delete student submission file: {}", e.getMessage());
                }
            }
        }

        // The cascade settings will handle all the database relationships
        assignmentRepository.delete(assignment);
    }

    @Transactional
    public Assignment gradeAssignment(Long assignmentId, GradeDTO gradeDTO, AppUser currentUser, Long studentId)
            throws AccessDeniedException {
        Assignment assignment = findById(assignmentId);

        if (currentUser.getRole() == Role.ROLE_STUDENT)
            throw new AccessDeniedException("Student can't grade assignments");

        // Validate that only teachers can grade their own assignments
        if (currentUser.getRole() == Role.ROLE_TEACHER &&
                !assignment.getAssignedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the assigned teacher can grade this assignment");
        }

        // Check if the assignment is past due and if any submission is not submitted
        /*
        boolean canGrade = assignment.getStudentSubmissions().stream()
                .noneMatch(studentSubmission -> studentSubmission.getStatus() != AssignmentStatus.SUBMITTED);
        //  && assignment.getDueDate().isBefore(LocalDate.now())

        if (!canGrade)
            throw new IllegalStateException("Can only grade assignments that have been submitted");
        */

        // Update the submission for the specific student
        assignment.getStudentSubmissions().stream()
                .filter(studentSubmission -> studentSubmission.getStudent().getId().equals(studentId))
                .forEach(studentSubmission -> {
                    studentSubmission.setGrade(gradeDTO.getGrade());
                    studentSubmission.setFeedback(gradeDTO.getFeedback());
                    studentSubmission.setStatus(AssignmentStatus.GRADED);
                });

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment unsubmitAssignment(Long assignmentId, AppUser currentUser) throws AccessDeniedException {
        AppUser user = appUserService.getCurrentUserWithDetails(currentUser.getId());
        Assignment assignment = findById(assignmentId);

        if (assignment.getDueDate().isBefore(LocalDate.now()))
            throw new IllegalStateException("Can only un-submit assignments that have been due.");

        if (user.getRole() != Role.ROLE_STUDENT)
            throw new AccessDeniedException("Only students can un-submit assignments");

        ClassEntity classEntity = user.getStudentDetails().getClassEntity();
        if (classEntity == null) {
            throw new EntityNotFoundException("Student is not assigned to any class");
        }

        // Verify the assignment belongs to the student's class through teacher courses
        boolean isEnrolled = teacherCourseRepository.existsByClassAndCourse(
                user.getStudentDetails().getClassEntity().getId(),
                assignment.getCourse().getId()
        );

        if (!isEnrolled)
            throw new AccessDeniedException("You can only un-submit your own assignments");

        StudentSubmission studentSubmission = assignment.getStudentSubmissions().stream()
                .filter(submission -> submission.getStudent().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Student didn't submit assignment"));

        if (studentSubmission.getStatus() != AssignmentStatus.SUBMITTED) {
            throw new IllegalStateException("Can only un-submit assignments that have been submitted.");
        }

        if (studentSubmission.getGrade() != null)
            throw new IllegalStateException("Cannot un-submit graded assignments");

        assignment.getStudentSubmissions().remove(studentSubmission);
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public StudentSubmission submitAssignment(Long assignmentId, SubmitAssignmentDTO submitDTO, AppUser currentUser)
            throws IllegalStateException, IOException {
        AppUser user = appUserService.getCurrentUserWithDetails(currentUser.getId());

        Assignment assignment = findById(assignmentId);

        if (LocalDate.now().isAfter(assignment.getDueDate()))
            throw new IllegalStateException("Assignment deadline has passed");

        Optional<StudentSubmission> optionalSubmission = assignment.getStudentSubmissions().stream()
                .filter(studentSubmission -> studentSubmission.getStudent().getId().equals(user.getId()))
                .findFirst();

        if (optionalSubmission.isPresent()) {
            StudentSubmission theStudentSubmission = optionalSubmission.get();
            if (theStudentSubmission.getStatus() == AssignmentStatus.SUBMITTED)
                throw new IllegalStateException("You have already submitted the assignment");
            if (theStudentSubmission.getGrade() != null || theStudentSubmission.getStatus() == AssignmentStatus.GRADED)
                throw new IllegalStateException("The assignment has already been graded");

            Files.deleteIfExists(Paths.get(theStudentSubmission.getDocument().getFilePath()));
            theStudentSubmission.setDocument(null);
            assignmentRepository.save(assignment);
        }

        ClassEntity classEntity = user.getStudentDetails().getClassEntity();
        if (classEntity == null) {
            throw new EntityNotFoundException("Student is not assigned to any class");
        }

        boolean isEnrolled = teacherCourseRepository.existsByClassAndCourse(
                user.getStudentDetails().getClassEntity().getId(),
                assignment.getCourse().getId()
        );

        if (!isEnrolled) {
            throw new AccessDeniedException("You can only submit your own assignments");
        }

        StudentSubmission studentSubmission = studentSubmissionService.submitAssignment(
                assignmentId,
                submitDTO,
                user
        );
        assignment.getStudentSubmissions().add(studentSubmission);
        assignmentRepository.save(assignment);
        return studentSubmission;
    }

    public Assignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
    }

    @Cacheable(value = "assignments", key = "#courseId")
    public List<AssignmentDTO> getAssignmentsByCourse(Long courseId, AppUser loggedInUser)
            throws AccessDeniedException {
        AppUser user = appUserRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Assignment> assignments;
        if (user.getRole() == Role.ROLE_TEACHER) {
            assignments = assignmentRepository.findByCourseIdAndAssignedBy(courseId, loggedInUser);
        } else {
            assignments = assignmentRepository.findByCourseIdOrderByDueDateDesc(courseId);
        }

        return assignments.stream()
                .map(assignment -> new AssignmentDTO(assignment, ""))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Assignment> createBatchAssignments(List<AssignmentRequestDTO> dtos, Long loggedInUserId)
            throws AccessDeniedException {
        return dtos.stream()
                .map(dto -> {
                    try {
                        return createAssignment(dto, loggedInUserId);
                    } catch (AccessDeniedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private void validateAssignmentRequest(AssignmentRequestDTO dto) {
        if (dto.getTitle().length() > maxTitleLength) {
            throw new IllegalArgumentException("Title exceeds maximum length");
        }

        if (dto.getDueDate().isBefore(LocalDate.now().plusDays(minDueDateDays))) {
            throw new IllegalArgumentException("Due date must be at least " + minDueDateDays + " days in the future");
        }
    }

    private void validateTeacherAccess(AppUser teacher, ClassEntity classEntity)
            throws AccessDeniedException {
        if (teacher.getRole() == Role.ROLE_STUDENT) {
            throw new AccessDeniedException("Only teachers, admins, coordinators can create assignments");
        }

        if (teacher.getRole() == Role.ROLE_TEACHER &&
                teacher.getTeacherDetails().getTeacherCourses().stream()
                        .noneMatch(tc -> tc.getClasses().stream()
                                .anyMatch(c -> c.getId().equals(classEntity.getId())))) {
            throw new AccessDeniedException("Teachers can create assignments only for their assigned classes");
        }
    }

    private Assignment createAssignmentEntity(
            AssignmentRequestDTO dto,
            AppUser teacher,
            ClassEntity classEntity,
            Course course,
            TeacherCourse teacherCourse) { // Add teacherCourse parameter
        return Assignment.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .assignedBy(teacher)
                .lastModifiedBy(teacher)
                .classEntity(classEntity)
                .course(course)
                .teacherCourse(teacherCourse) // Set the teacherCourse
                .date(LocalDate.now())
                .lastModified(LocalDate.now())
                .studentSubmissions(new ArrayList<>())
                .build();
    }
}