package com.lsm.model.DTOs;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeacherResponseDTO extends UserResponseDTO {
    private String phone;
    private String tc;
    private LocalDate birthDate;
    private List<TeacherCourseResponseDTO> teacherCourses;
}
