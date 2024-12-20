package com.lsm.model.DTOs;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeacherUpdateRequestDTO extends UserUpdateRequestDTO {
    private String phone;
    private List<Long> classIds;
    private List<Long> courseIds;
}
