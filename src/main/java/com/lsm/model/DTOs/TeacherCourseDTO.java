package com.lsm.model.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCourseDTO {
    @NotNull
    private long teacherId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Class IDs are required")
    private List<Long> classIds;
}
