package com.lsm.model.DTOs;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResultResponseDTO {
    private Long id;
    private String subjectName;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Integer blankAnswers;
    private Double netScore;
}
