package com.lsm.model.entity;

import com.lsm.model.validation.constraint.TCConstraint;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDetails {
    private String phone;
    @TCConstraint
    private String tc;
    private LocalDate birthDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "teacher")
    @Builder.Default
    private Set<TeacherCourse> teacherCourses = new HashSet<>();
}
