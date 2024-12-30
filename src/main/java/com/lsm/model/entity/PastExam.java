package com.lsm.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "past_exams")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastExam {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "past_exam_seq")
    @SequenceGenerator(
            name = "past_exam_seq",
            sequenceName = "past_exams_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamType examType;

    private Double overallAverage;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentExamResult> results = new HashSet<>();

    public enum ExamType {
        TYT, AYT, YDT
    }
}
