package com.lsm.model.entity;

import com.lsm.model.entity.base.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teacher_courses")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "teacher_course_seq")
    @SequenceGenerator(name = "teacher_course_seq", sequenceName = "teacher_course_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private AppUser teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToMany
    @JoinTable(
            name = "teacher_course_classes",
            joinColumns = @JoinColumn(name = "teacher_course_id"),
            inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    @Builder.Default
    private Set<ClassEntity> classes = new HashSet<>();
}
