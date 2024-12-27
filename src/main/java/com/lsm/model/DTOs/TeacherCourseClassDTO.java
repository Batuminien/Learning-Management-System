package com.lsm.model.DTOs;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TeacherCourseClassDTO {
    private Long courseId;
    private List<Long> classIds;
}
