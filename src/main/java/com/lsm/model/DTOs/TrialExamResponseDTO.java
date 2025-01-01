package com.lsm.model.DTOs;

import com.lsm.model.entity.PastExam;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TrialExamResponseDTO {
    private String examName;
    private LocalDate examDate;
    @Enumerated(EnumType.STRING) private PastExam.ExamType examType;
    private String resultPdfUrl;
}
