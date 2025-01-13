package com.lsm.repository;

import com.lsm.model.entity.Course;
import com.lsm.model.entity.TeacherCourse;
import com.lsm.model.entity.base.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeacherCourseRepository extends JpaRepository<TeacherCourse, Long> {
    Optional<TeacherCourse> findByTeacherAndCourse(AppUser teacher, Course course);
    @Query("SELECT COUNT(tc) > 0 FROM TeacherCourse tc " +
            "JOIN tc.classes c " +
            "WHERE c.id = :classId AND tc.course.id = :courseId")
    boolean existsByClassAndCourse(@Param("classId") Long classId, @Param("courseId") Long courseId);
    boolean existsByTeacherId(Long teacherId);
    boolean existsByIdAndTeacherId(Long id, Long teacherId);
    @Query("SELECT tc FROM TeacherCourse tc " +
            "JOIN tc.classes c " +
            "WHERE tc.course.id = :courseId " +
            "AND c.id = :classId")
    Optional<TeacherCourse> findByCourseAndClass(@Param("courseId") Long courseId,
                                                 @Param("classId") Long classId);
}
