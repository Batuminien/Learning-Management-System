package com.lsm.model.DTOs;

import com.lsm.model.entity.PastExam.ExamType;
import lombok.*;
import jakarta.validation.constraints.*;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastExamRequestDTO {
    @NotBlank(message = "Exam name is required")
    private String name;

    @NotNull(message = "Exam type is required")
    private ExamType examType;

    private Set<StudentExamResultRequestDTO> results;
}
