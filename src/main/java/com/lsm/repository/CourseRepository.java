package com.lsm.repository;

import com.lsm.model.entity.Course;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT c FROM Course c " +
            "LEFT JOIN FETCH c.teacherCourses tc " +
            "LEFT JOIN FETCH tc.teacher " +
            "LEFT JOIN FETCH tc.classes " +
            "WHERE c.id = :id")
    Optional<Course> findByIdWithClasses(Long id);

    @Query("SELECT DISTINCT c FROM Course c " +
            "LEFT JOIN FETCH c.assignments " +
            "LEFT JOIN FETCH c.teacherCourses tc " +
            "LEFT JOIN FETCH tc.teacher " +
            "LEFT JOIN FETCH tc.classes")
    List<Course> findAllWithAssignments();

    @Query("SELECT c FROM Course c " +
            "JOIN c.teacherCourses tc " +
            "JOIN tc.classes cl " +
            "WHERE cl.id = :classId")
    List<Course> findByClassId(Long classId);

    Optional<Course> findCourseByName(String courseName);

    Set<Course> findAllByIdIn(List<Long> ids);

    @Query("SELECT c FROM Course c " +
            "JOIN c.teacherCourses tc " +
            "WHERE tc.teacher.id = :teacherId")
    List<Course> findByTeacherId(Long teacherId);

    List<Course> findAll(Specification<Course> spec);
}