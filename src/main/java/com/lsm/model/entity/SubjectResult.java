package com.lsm.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subject_results")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResult {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subject_result_seq")
    @SequenceGenerator(
            name = "subject_result_seq",
            sequenceName = "subject_results_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_result_id", nullable = false)
    private StudentExamResult examResult;

    @Column(nullable = false)
    private String subjectName;

    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Integer blankAnswers;
    private Double netScore;
}
