package com.lsm.model.DTOs;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class ClassEntityResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private List<Long> studentIds;
    private List<Long> assignmentIds;
}
