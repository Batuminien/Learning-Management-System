package com.lsm.model.DTOs;

import com.lsm.model.entity.PastExam;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastExamResponseDTO {
    private Long id;
    private String name;
    private PastExam.ExamType examType;
    private Double overallAverage;
    private LocalDateTime date;
    private Set<StudentExamResultResponseDTO> results;
}
