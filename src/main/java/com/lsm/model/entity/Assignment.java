package com.lsm.model.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.AssignmentStatus;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotNull
    @Column(name = "assignment_date", nullable = false)
    private LocalDate date;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private Set<AssignmentDocument> teacherDocuments = new HashSet<>();

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private Set<AssignmentDocument> studentSubmissions = new HashSet<>();
}