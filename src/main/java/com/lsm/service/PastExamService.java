package com.lsm.service;

import com.lsm.model.DTOs.*;
import com.lsm.model.entity.PastExam;
import com.lsm.model.entity.StudentExamResult;
import com.lsm.model.entity.SubjectResult;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.PastExamRepository;
import com.lsm.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PastExamService {

    private final PastExamRepository pastExamRepository;
    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;

    @Autowired
    public PastExamService(PastExamRepository pastExamRepository, AppUserRepository appUserRepository, AppUserService appUserService) {
        this.pastExamRepository = pastExamRepository;
        this.appUserRepository = appUserRepository;
        this.appUserService = appUserService;
    }

    @Transactional
    public PastExamResponseDTO createPastExam(AppUser creator, PastExamRequestDTO requestDTO) throws AccessDeniedException {
        validateTeacherAccess(creator);

        PastExam exam = PastExam.builder()
                .name(requestDTO.getName())
                .examType(requestDTO.getExamType())
                .results(new HashSet<>())
                .build();

        if (requestDTO.getResults() != null && !requestDTO.getResults().isEmpty()) {
            Set<StudentExamResult> results = processStudentResults(requestDTO.getResults(), exam);
            exam.setResults(results);
            calculateAndSetOverallAverage(exam);
        }

        PastExam savedExam = pastExamRepository.save(exam);
        return mapToResponseDTO(savedExam);
    }

    @Transactional(readOnly = true)
    public PastExamResponseDTO getPastExamById(AppUser requester, Long examId) throws AccessDeniedException {
        PastExam exam = pastExamRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Past exam not found with ID: " + examId));

        if (requester.getRole() == Role.ROLE_STUDENT) {
            validateStudentAccess(requester, exam);
        }

        return mapToResponseDTO(exam);
    }

    @Transactional(readOnly = true)
    public List<PastExamResponseDTO> getAllPastExams() {
        return pastExamRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentExamResultResponseDTO> getStudentResults(AppUser requester, Long studentId)
            throws AccessDeniedException {
        if (requester.getRole() == Role.ROLE_STUDENT && !requester.getId().equals(studentId)) {
            throw new AccessDeniedException("Students can only view their own results");
        }

        AppUser student = appUserRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));
        student = appUserService.getCurrentUserWithDetails(studentId);

        List<StudentExamResult> results = pastExamRepository.findAllResultsByStudentId(studentId);
        return results.stream()
                .map(this::mapToStudentResultResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PastExamResponseDTO updatePastExam(AppUser updater, Long examId, PastExamRequestDTO requestDTO)
            throws AccessDeniedException {
        validateTeacherAccess(updater);

        PastExam exam = pastExamRepository.findById(examId)
                .orElseThrow(() -> new EntityNotFoundException("Past exam not found with ID: " + examId));

        exam.setName(requestDTO.getName());
        exam.setExamType(requestDTO.getExamType());

        if (requestDTO.getResults() != null) {
            exam.getResults().clear();
            Set<StudentExamResult> results = processStudentResults(requestDTO.getResults(), exam);
            exam.setResults(results);
            calculateAndSetOverallAverage(exam);
        }

        PastExam updatedExam = pastExamRepository.save(exam);
        return mapToResponseDTO(updatedExam);
    }

    @Transactional
    public void deletePastExam(AppUser deleter, Long examId) throws AccessDeniedException {
        validateTeacherAccess(deleter);

        if (!pastExamRepository.existsById(examId)) {
            throw new EntityNotFoundException("Past exam not found with ID: " + examId);
        }

        pastExamRepository.deleteById(examId);
    }

    private Set<StudentExamResult> processStudentResults(Set<StudentExamResultRequestDTO> resultDTOs, PastExam exam) {
        return resultDTOs.stream()
                .map(resultDTO -> createStudentExamResult(resultDTO, exam))
                .collect(Collectors.toSet());
    }

    private StudentExamResult createStudentExamResult(StudentExamResultRequestDTO resultDTO, PastExam exam) {
        AppUser student = appUserRepository.findById(resultDTO.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + resultDTO.getStudentId()));

        StudentExamResult result = StudentExamResult.builder()
                .exam(exam)
                .student(student)
                .subjectResults(new HashSet<>())
                .build();

        if (resultDTO.getSubjectResults() != null) {
            Set<SubjectResult> subjectResults = resultDTO.getSubjectResults().stream()
                    .map(subjectDTO -> createSubjectResult(subjectDTO, result))
                    .collect(Collectors.toSet());
            result.setSubjectResults(subjectResults);
        }

        return result;
    }

    private SubjectResult createSubjectResult(SubjectResultRequestDTO subjectDTO, StudentExamResult examResult) {
        double netScore = calculateNetScore(
                subjectDTO.getCorrectAnswers(),
                subjectDTO.getIncorrectAnswers()
        );

        return SubjectResult.builder()
                .examResult(examResult)
                .subjectName(subjectDTO.getSubjectName())
                .correctAnswers(subjectDTO.getCorrectAnswers())
                .incorrectAnswers(subjectDTO.getIncorrectAnswers())
                .blankAnswers(subjectDTO.getBlankAnswers())
                .netScore(netScore)
                .build();
    }

    private double calculateNetScore(int correct, int incorrect) {
        return correct - (incorrect * 0.25); // Standard YKS scoring formula
    }

    private void calculateAndSetOverallAverage(PastExam exam) {
        if (exam.getResults().isEmpty()) {
            exam.setOverallAverage(0.0);
            return;
        }

        double totalNet = exam.getResults().stream()
                .flatMap(result -> result.getSubjectResults().stream())
                .mapToDouble(SubjectResult::getNetScore)
                .sum();

        int totalSubjects = exam.getResults().stream()
                .mapToInt(result -> result.getSubjectResults().size())
                .sum();

        exam.setOverallAverage(totalNet / totalSubjects);
    }

    private void validateTeacherAccess(AppUser user) throws AccessDeniedException {
        if (user.getRole() != Role.ROLE_TEACHER &&
                user.getRole() != Role.ROLE_ADMIN &&
                user.getRole() != Role.ROLE_COORDINATOR) {
            throw new AccessDeniedException("Only teachers, admins, and coordinators can perform this operation");
        }
    }

    private void validateStudentAccess(AppUser student, PastExam exam) throws AccessDeniedException {
        boolean hasAccess = exam.getResults().stream()
                .anyMatch(result -> result.getStudent().getId().equals(student.getId()));

        if (!hasAccess) {
            throw new AccessDeniedException("Student does not have access to this exam");
        }
    }

    private PastExamResponseDTO mapToResponseDTO(PastExam exam) {
        return PastExamResponseDTO.builder()
                .id(exam.getId())
                .name(exam.getName())
                .examType(exam.getExamType())
                .overallAverage(exam.getOverallAverage())
                .date(exam.getExamDate())
                .results(exam.getResults().stream()
                        .map(this::mapToStudentResultResponseDTO)
                        .collect(Collectors.toSet()))
                .build();
    }

    private PastExamBasicDTO mapToBasicDTO(PastExam exam) {
        return PastExamBasicDTO.builder()
                .id(exam.getId())
                .name(exam.getName())
                .examType(exam.getExamType())
                .overallAverage(exam.getOverallAverage())
                .date(exam.getExamDate())
                .build();
    }

    private StudentExamResultResponseDTO mapToStudentResultResponseDTO(StudentExamResult result) {
        return StudentExamResultResponseDTO.builder()
                .id(result.getId())
                .studentId(result.getStudent().getId())
                .studentName(result.getStudent().getName() + " " + result.getStudent().getSurname())
                .pastExam(mapToBasicDTO(result.getExam()))  // Use the basic DTO here
                .subjectResults(result.getSubjectResults().stream()
                        .map(this::mapToSubjectResultResponseDTO)
                        .collect(Collectors.toSet()))
                .build();
    }

    private SubjectResultResponseDTO mapToSubjectResultResponseDTO(SubjectResult result) {
        return SubjectResultResponseDTO.builder()
                .id(result.getId())
                .subjectName(result.getSubjectName())
                .correctAnswers(result.getCorrectAnswers())
                .incorrectAnswers(result.getIncorrectAnswers())
                .blankAnswers(result.getBlankAnswers())
                .netScore(result.getNetScore())
                .build();
    }
}
