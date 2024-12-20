package com.lsm.model.DTOs;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudentResponseDTO extends UserResponseDTO {
    private String phone;
    private String tc;
    private LocalDate birthDate;
    private LocalDate registrationDate;
    private String parentName;
    private String parentPhone;
    private Long classId;
}

