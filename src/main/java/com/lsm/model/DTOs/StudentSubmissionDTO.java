package com.lsm.model.DTOs;

import com.lsm.mapper.AssignmentDocumentMapper;
import com.lsm.model.entity.StudentSubmission;
import com.lsm.model.entity.enums.AssignmentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSubmissionDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private AssignmentStatus status;
    private AssignmentDocumentDTO document;
    private LocalDateTime submissionDate;
    private String comment;
    private Double grade;
    private String feedback;

    public StudentSubmissionDTO(StudentSubmission submission) {
        this.id = submission.getId();
        this.studentId = submission.getStudent().getId();
        this.studentName = submission.getStudent().getUsername();
        this.status = submission.getStatus();
        this.submissionDate = submission.getSubmissionDate();
        this.comment = submission.getComment();
        this.grade = submission.getGrade();
        this.feedback = submission.getFeedback();

        if (submission.getDocument() != null) {
            AssignmentDocumentMapper assignmentDocumentMapper = new AssignmentDocumentMapper();
            this.document = assignmentDocumentMapper.convertToDTO(submission.getDocument());
        }
    }
}
