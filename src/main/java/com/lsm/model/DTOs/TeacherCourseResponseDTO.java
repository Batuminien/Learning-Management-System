package com.lsm.model.DTOs;

import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class TeacherCourseResponseDTO {
    private Long teacherId;
    private Long courseId;
    private List<Long> classIds;
}
