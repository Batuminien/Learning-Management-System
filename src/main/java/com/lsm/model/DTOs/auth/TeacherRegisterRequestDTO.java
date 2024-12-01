package com.lsm.model.DTOs.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TeacherRegisterRequestDTO extends RegisterRequestDTO {
    private String phone;
    private String tc;
    private LocalDate birthDate;
    private List<Long> classes;
    private List<Long> courses;
}
