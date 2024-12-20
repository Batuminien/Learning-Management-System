package com.lsm.model.DTOs;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StudentUpdateRequestDTO extends UserUpdateRequestDTO {
    private String phone;
    private String parentName;
    private String parentPhone;
    private Long classId;
}
