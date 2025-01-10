package com.lsm.model.DTOs;

import com.lsm.model.entity.PastExam;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastExamBasicDTO {
    private Long id;
    private String name;
    private PastExam.ExamType examType;
    private Double overallAverage;
}
