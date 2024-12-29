package com.lsm.model.DTOs;

import java.util.List;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceStatsDTO {
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private Long courseId;
    private String courseName;
    private double attendancePercentage;
    private Long totalClasses;
    private Long presentCount;
    private Long absentCount;
    private Long lateCount;
    private Long excusedCount;
    private List<AttendanceDTO> recentAttendance;
}
