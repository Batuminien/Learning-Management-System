package com.lsm.model.DTOs;

import com.lsm.model.entity.enums.AttendanceStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AttendanceRequestDTO {
    @NotNull(message = "Student ID cannot be null")
    private Long studentId;
    
    @NotNull(message = "Date cannot be null")
    private LocalDate date;
    
    @NotNull(message = "Attendance status cannot be null")
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private String comment;

    @NotNull(message = "Class ID cannot be null")
    private Long classId;

    @NotNull(message = "Course ID cannot be null")
    private Long courseId;
}
