package com.lsm.repository;

import com.lsm.model.entity.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {
    List<CourseSchedule> findByTeacherCourseId(Long teacherCourseId);

    List<CourseSchedule> findByClassEntityId(Long classId);

    @Query("SELECT cs FROM CourseSchedule cs " +
            "WHERE cs.teacherCourse.teacher.id = :teacherId")
    List<CourseSchedule> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT cs FROM CourseSchedule cs " +
            "JOIN cs.classEntity ce " +
            "JOIN ce.students s " +
            "WHERE s.id = :studentId")
    List<CourseSchedule> findByStudentId(@Param("studentId") Long studentId);
}
