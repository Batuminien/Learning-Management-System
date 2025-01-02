package com.lsm.model.DTOs;

import lombok.Data;
import lombok.Builder;

import java.util.Map;

@Data
@Builder
public class TeacherCourseResponseDTO {
    private Long teacherId;
    private Long courseId;
    private String courseName;
    private Map<Long, String> classIdsAndNames;
}
