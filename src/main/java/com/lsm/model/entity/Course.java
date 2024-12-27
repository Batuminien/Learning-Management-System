package com.lsm.model.entity;

import com.lsm.model.entity.base.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_seq")
    @SequenceGenerator(name = "course_seq", sequenceName = "courses_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "credits")
    private Integer credits;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Assignment> assignments = new HashSet<>();

    @OneToMany(mappedBy = "course")
    @Builder.Default
    private Set<TeacherCourse> teacherCourses = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "course_classes",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    private List<ClassEntity> classes;
}
