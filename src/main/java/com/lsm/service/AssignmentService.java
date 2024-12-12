package com.lsm.service;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.lsm.model.DTOs.*;
import com.lsm.model.entity.StudentSubmission;
import com.lsm.model.entity.AssignmentDocument;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.Course;
import com.lsm.model.entity.enums.AssignmentStatus;
import com.lsm.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import com.lsm.model.entity.Assignment;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;

import jakarta.transaction.Transactional;
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

    @Value("${assignment.max-title-length:100}")
    private int maxTitleLength;

    @Value("${assignment.min-due-date-days:1}")
    private int minDueDateDays;

    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepository, AppUserRepository appUserRepository, ClassEntityRepository classEntityRepository, AssignmentDocumentRepository assignmentDocumentRepository, StudentSubmissionRepository studentSubmissionRepository, CourseRepository courseRepository, AppUserService appUserService, AssignmentDocumentService assignmentDocumentService, StudentSubmissionService studentSubmissionService) {
        this.assignmentRepository = assignmentRepository;
        this.appUserRepository = appUserRepository;
        this.classEntityRepository = classEntityRepository;
        this.assignmentDocumentRepository = assignmentDocumentRepository;
        this.studentSubmissionRepository = studentSubmissionRepository;
        this.courseRepository = courseRepository;
        this.appUserService = appUserService;
        this.assignmentDocumentService = assignmentDocumentService;
        this.studentSubmissionService = studentSubmissionService;
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

            ClassEntity classEntity = classEntityRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found"));

            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException("Course not found"));

            validateTeacherAccess(teacher, classEntity);
            // validateUniqueTitle(dto.getTitle(), classEntity); // No existingAssignmentId for creation

            Assignment assignment = createAssignmentEntity(dto, teacher, classEntity, course);

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
        AppUser  teacher = appUserRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        // Validate that user is actually a teacher
        if (teacher.getRole() != Role.ROLE_TEACHER) {
            throw new IllegalArgumentException("Specified user is not a teacher");
        }

        if (!loggedInUser.getId().equals(teacher.getId()))
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

        // Get student's class
        ClassEntity classEntity = classEntityRepository.findById(student.getStudentDetails().getClassEntity())
                .orElseThrow(() -> new EntityNotFoundException("Class not found by id: " + student.getStudentDetails().getClassEntity()));

        // Get all assignments for the student's class
        List<Assignment> assignments = assignmentRepository.findByClassEntityOrderByDueDateDesc(classEntity);

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

        // Clear teacher document
        if (assignment.getTeacherDocument() != null) {
            AssignmentDocument teacherDoc = assignment.getTeacherDocument();
            assignment.setTeacherDocument(null);
            assignmentDocumentRepository.delete(teacherDoc);
        }

        // Clear student submissions
        if (!assignment.getStudentSubmissions().isEmpty()) {
            assignment.getStudentSubmissions().forEach(submission -> {
                submission.setAssignment(null);
                studentSubmissionRepository.delete(submission);
            });
            assignment.getStudentSubmissions().clear();
        }

        // Remove assignment from class entities
        List<Long> classIds = user.getTeacherDetails().getClasses().stream()
                .map(ClassEntity::getId)
                .collect(Collectors.toList());

        Set<ClassEntity> classEntities = classEntityRepository.findAllByIdIn(classIds);

        classEntities.forEach(classEntity -> {
            classEntity.getAssignments().remove(assignment);
            classEntityRepository.save(classEntity);
        });

        // Finally delete the assignment
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
        boolean canGrade = assignment.getStudentSubmissions().stream()
                .noneMatch(studentSubmission -> studentSubmission.getStatus() != AssignmentStatus.SUBMITTED)
        /*  && assignment.getDueDate().isBefore(LocalDate.now()) */;

        if (!canGrade)
            throw new IllegalStateException("Can only grade assignments that have been submitted");

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
    public Assignment unsubmitAssignment(Long assignmentId, AppUser  currentUser) throws AccessDeniedException {
        Assignment assignment = findById(assignmentId);

        if (assignment.getDueDate().isBefore(LocalDate.now()))
            throw new IllegalStateException("Can only un-submit assignments that have been due.");

        // Validate that only the student can un-submit their assignment
        if (currentUser .getRole() != Role.ROLE_STUDENT)
            throw new AccessDeniedException("Only students can un-submit assignments");

        // Check if the class entity exists
        ClassEntity classEntity = classEntityRepository.findById(currentUser .getStudentDetails().getClassEntity())
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));

        // Verify the assignment belongs to the student
        if (!classEntity.getCourses().contains(assignment.getCourse()))
            throw new AccessDeniedException("You can only un-submit your own assignments");

        // Find the student's submission
        StudentSubmission studentSubmission = assignment.getStudentSubmissions().stream()
                .filter(submission -> submission.getStudent().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Student didn't submit assignment"));

        // Validate submission status
        if (studentSubmission.getStatus() != AssignmentStatus.SUBMITTED) {
            throw new IllegalStateException("Can only un-submit assignments that have been submitted.");
        }

        // Validate that assignment is in SUBMITTED status and not yet graded
        if (studentSubmission.getGrade() != null)
            throw new IllegalStateException("Cannot un-submit graded assignments");

        assignment.getStudentSubmissions().remove(studentSubmission);

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public StudentSubmission submitAssignment(Long assignmentId, SubmitAssignmentDTO submitDTO, AppUser currentUser)
            throws IllegalStateException, IOException {
        Assignment assignment = findById(assignmentId);

        // Check deadline
        if (LocalDate.now().isAfter(assignment.getDueDate()))
            throw new IllegalStateException("Assignment deadline has passed");

        Optional<StudentSubmission> optionalSubmission = assignment.getStudentSubmissions().stream()
                .filter(studentSubmission -> studentSubmission.getStudent().getId().equals(currentUser .getId()))
                .findFirst();

        if (optionalSubmission.isPresent()) {
            StudentSubmission theStudentSubmission = optionalSubmission.get();

            if (theStudentSubmission.getStatus() == AssignmentStatus.SUBMITTED)
                throw new IllegalStateException("You have already submitted the assignment");
            else if (theStudentSubmission.getGrade() != null)
                throw new IllegalStateException("The assignment has already been graded");
            else if (theStudentSubmission.getStatus() == AssignmentStatus.GRADED)
                throw new IllegalStateException("The assignment has already been graded");
            else {
                // Delete the old file
                Files.deleteIfExists(Paths.get(theStudentSubmission.getDocument().getFilePath()));
                theStudentSubmission.setDocument(null);
                assignmentRepository.save(assignment);
            }
        }

        Optional<ClassEntity> classEntityOpt = classEntityRepository.findById(currentUser.getStudentDetails().getClassEntity());
        if (classEntityOpt.isEmpty())
            throw new EntityNotFoundException("Class not found");
        ClassEntity classEntity = classEntityOpt.get();

        // Verify the assignment belongs to the student
        if (!classEntity.getCourses().contains(assignment.getCourse())) {
            throw new AccessDeniedException("You can only submit your own assignments");
        }

        // Upload document
        StudentSubmission studentSubmission = studentSubmissionService.submitAssignment(
                assignmentId,
                submitDTO,
                currentUser
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
                !teacher.getTeacherDetails().getClasses().contains(classEntity)) {
            throw new AccessDeniedException("Teachers can create assignments only for their assigned classes");
        }
    }

    private Assignment createAssignmentEntity(
            AssignmentRequestDTO dto,
            AppUser teacher,
            ClassEntity classEntity,
            Course course) {
        Assignment assignment = new Assignment();
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setAssignedBy(teacher);
        if(dto.getDocument() != null)
            assignment.setTeacherDocument(dto.getDocument().DTOtoDocument(assignmentRepository, appUserRepository));
        assignment.setClassEntity(classEntity);
        assignment.setCourse(course);
        assignment.setDate(LocalDate.now());

        return assignment;
    }
}