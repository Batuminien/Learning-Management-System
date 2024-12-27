package com.lsm.model.DTOs;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassEntityResponseDTO {
    private Long id;
    private String name;
    private String description;
    private List<TeacherCourseResponseDTO> teacherCourses;
    private Map<Long, String> studentIdAndNames;
    private List<Long> assignmentIds;
}


