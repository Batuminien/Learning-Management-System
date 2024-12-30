package com.lsm.model.DTOs;

import lombok.*;
import jakarta.validation.constraints.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentExamResultRequestDTO {
    @NotNull(message = "Student ID is required")
    private Long studentId;
    private Set<SubjectResultRequestDTO> subjectResults;
}
