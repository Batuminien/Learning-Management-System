package com.lsm.model.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassEntityRequestDTO {
    @NotBlank(message = "Class name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private List<Long> studentIds;
    private List<TeacherCourseDTO> teacherCourses;
}

