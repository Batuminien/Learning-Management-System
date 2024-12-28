package com.lsm.model.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.lsm.model.entity.base.AppUser;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assignment_seq")
    @SequenceGenerator(name = "assignment_seq", sequenceName = "assignments_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "assigned_by_teacher_id", nullable = false)
    private AppUser assignedBy;

    @ManyToOne
    @JoinColumn(name = "last_modified_by_id", nullable = false)
    private AppUser lastModifiedBy;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @NotNull
    @Column(name = "assignment_date", nullable = false)
    private LocalDate date;

    @NotNull
    @Column(name = "last_modified_date", nullable = false)
    private LocalDate lastModified;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "teacher_document_id", referencedColumnName = "id")
    private AssignmentDocument teacherDocument;

    @Builder.Default
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentSubmission> studentSubmissions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "teacher_course_id", nullable = false)
    private TeacherCourse teacherCourse;

    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDate.now();
        }
        if (lastModified == null) {
            lastModified = LocalDate.now();
        }
        if (studentSubmissions == null) {
            studentSubmissions = new ArrayList<>();
        }
    }
}