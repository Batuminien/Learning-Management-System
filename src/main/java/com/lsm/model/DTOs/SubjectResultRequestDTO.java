package com.lsm.model.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResultRequestDTO {
    @NotBlank(message = "Subject name is required")
    private String subjectName;

    @Min(0)
    private Integer correctAnswers;

    @Min(0)
    private Integer incorrectAnswers;

    @Min(0)
    private Integer blankAnswers;
}
