package com.lsm.model.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseScheduleDTO {
    private Long id;

    @NotNull(message = "Teacher course ID is required")
    private Long teacherCourseId;

    private String teacherName;

    private String teacherCourseName;

    @NotNull(message = "Class ID is required")
    private Long classId;

    private String className;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Location is required")
    private String location;
}
