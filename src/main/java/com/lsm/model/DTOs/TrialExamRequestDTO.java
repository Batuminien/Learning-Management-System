package com.lsm.model.DTOs;

import com.lsm.model.entity.PastExam;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
public class TrialExamRequestDTO {
    @Size(max = 100, message = "Exam name must not exceed 100 characters")
    private String examName;
    private LocalDate examDate;
    @NotNull private MultipartFile results;
    @NotNull private MultipartFile answerKey;
    @NotNull @Enumerated(EnumType.STRING) private PastExam.ExamType examType;
}
