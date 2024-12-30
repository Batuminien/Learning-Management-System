package com.lsm.model.DTOs;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentExamResultResponseDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Set<SubjectResultResponseDTO> subjectResults;
}
