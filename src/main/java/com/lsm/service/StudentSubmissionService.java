package com.lsm.service;

import com.lsm.model.DTOs.SubmitAssignmentDTO;
import com.lsm.model.entity.*;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.AssignmentStatus;
import com.lsm.repository.AssignmentRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.repository.StudentSubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentSubmissionService {

    private final StudentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ClassEntityRepository classEntityRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public StudentSubmission submitAssignment(Long assignmentId, SubmitAssignmentDTO submitDTO, AppUser student)
            throws IOException {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        // Verify submission deadline
        validateSubmissionDeadline(assignment);

        // Verify student's class enrollment
        validateStudentEnrollment(student, assignment);

        // Handle document upload first to get AssignmentDocument
        AssignmentDocument document = fileStorageService.handleDocumentUpload(
                submitDTO.getDocument(),
                assignment,
                student
        );

        // Get or create submission with the new document
        StudentSubmission submission = getOrCreateSubmission(document, assignment, student);

        // Validate submission status
        validateSubmissionStatus(submission);

        // Update submission details
        submission.setStatus(AssignmentStatus.SUBMITTED);
        submission.setSubmissionDate(LocalDate.now());
        submission.setComment(submitDTO.getSubmissionComment());

        return submissionRepository.save(submission);
    }

    private void validateSubmissionDeadline(Assignment assignment) {
        if (LocalDate.now().isAfter(assignment.getDueDate())) {
            throw new IllegalStateException("Assignment deadline has passed");
        }
    }

    private void validateStudentEnrollment(AppUser student, Assignment assignment) throws AccessDeniedException {
        ClassEntity studentClass = classEntityRepository.findById(student.getStudentDetails().getClassEntity())
                .orElseThrow(() -> new EntityNotFoundException("Student's class not found"));

        if (!studentClass.getCourses().contains(assignment.getCourse())) {
            throw new AccessDeniedException("You can only submit assignments for your enrolled courses");
        }
    }

    private StudentSubmission getOrCreateSubmission(AssignmentDocument doc, Assignment assignment, AppUser student) {
        return assignment.getStudentSubmissions().stream()
                .filter(sub -> sub.getStudent().getId().equals(student.getId()))
                .findFirst()
                .orElse(StudentSubmission.builder()
                        .assignment(assignment)
                        .document(doc)
                        .student(student)
                        .status(AssignmentStatus.PENDING)
                        .build());
    }

    private void validateSubmissionStatus(StudentSubmission submission) {
        if (submission.getStatus() == AssignmentStatus.SUBMITTED) {
            throw new IllegalStateException("You have already submitted this assignment");
        }
        if (submission.getStatus() == AssignmentStatus.GRADED || submission.getGrade() != null) {
            throw new IllegalStateException("This assignment has already been graded");
        }
    }

    public StudentSubmission findSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
    }

    public StudentSubmission findStudentSubmission(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignment_IdAndStudent_Id(assignmentId, studentId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
    }

    @Transactional
    public void deleteSubmission(Long submissionId) throws IOException {
        StudentSubmission submission = findSubmissionById(submissionId);
        if (submission.getDocument() != null) {
            Files.deleteIfExists(Paths.get(submission.getDocument().getFilePath()));
        }
        submissionRepository.delete(submission);
    }
}