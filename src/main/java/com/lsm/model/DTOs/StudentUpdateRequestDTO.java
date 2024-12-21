package com.lsm.model.DTOs;

import com.lsm.model.validation.constraint.TCConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class StudentUpdateRequestDTO extends UserUpdateRequestDTO {
    private String phone;
    @TCConstraint
    private String tc;
    private LocalDate birthDate;
    private LocalDate registrationDate;
    private String parentName;
    private String parentPhone;
    private Long classId;
}
