package com.lsm.model.entity;

import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.AssignmentStatus;
import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "student_submissions")
public class StudentSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submission_seq")
    @SequenceGenerator(name = "submission_seq", sequenceName = "submissions_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default private AssignmentStatus status = AssignmentStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "document_id", referencedColumnName = "id")
    private AssignmentDocument document;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "submission_comment")
    private String comment;

    @Column(name = "grade")
    private Double grade;

    @Column(name = "feedback")
    private String feedback;
}
