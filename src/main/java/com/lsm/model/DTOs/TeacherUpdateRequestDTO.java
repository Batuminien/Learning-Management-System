package com.lsm.model.DTOs;

import com.lsm.model.validation.constraint.TCConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeacherUpdateRequestDTO extends UserUpdateRequestDTO {
    private String phone;
    @TCConstraint
    private String tc;
    private LocalDate birthDate;
    private List<Long> classIds;
    private List<Long> courseIds;
}
