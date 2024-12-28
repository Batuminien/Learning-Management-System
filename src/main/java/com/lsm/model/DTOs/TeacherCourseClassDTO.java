package com.lsm.model.DTOs;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
public class TeacherCourseClassDTO {
    private final Long courseId;
    private final List<Long> classIds;

    @JsonCreator
    public TeacherCourseClassDTO(
            @JsonProperty("courseId") Long courseId,
            @JsonProperty("classIds") List<Long> classIds
    ) {
        this.courseId = courseId;
        this.classIds = classIds;
    }
}
