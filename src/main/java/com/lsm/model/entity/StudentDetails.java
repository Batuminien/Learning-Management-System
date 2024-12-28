package com.lsm.model.entity;

import java.time.LocalDate;

import com.lsm.model.validation.constraint.TCConstraint;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetails {
    private String phone;
    @TCConstraint
    private String tc;
    private LocalDate birthDate;
    private LocalDate registrationDate;
    private String parentName;
    private String parentPhone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id_student")
    private ClassEntity classEntity;
}
