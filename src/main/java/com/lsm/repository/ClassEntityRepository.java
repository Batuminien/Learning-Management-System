package com.lsm.repository;

import com.lsm.model.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {
    @Query("SELECT DISTINCT c FROM ClassEntity c " +
            "LEFT JOIN FETCH c.assignments " +
            "LEFT JOIN FETCH c.students " +
            "LEFT JOIN FETCH c.teacherCourses tc " +
            "LEFT JOIN FETCH tc.course " +
            "LEFT JOIN FETCH tc.classes")
    List<ClassEntity> findAllWithAssociations();

    Optional<ClassEntity> findClassEntityByName(String className);

    Optional<ClassEntity> getClassEntityById(Long id);

    @Query("SELECT DISTINCT c FROM ClassEntity c " +
            "LEFT JOIN FETCH c.assignments a " +
            "LEFT JOIN FETCH a.assignedBy " +
            "LEFT JOIN FETCH a.course " +
            "LEFT JOIN FETCH a.lastModifiedBy " +
            "LEFT JOIN FETCH a.teacherDocument " +
            "LEFT JOIN FETCH c.students " +
            "LEFT JOIN FETCH c.teacherCourses tc " +
            "LEFT JOIN FETCH tc.teacher " +
            "LEFT JOIN FETCH tc.course " +
            "WHERE c.id = :id")
    Optional<ClassEntity> findByIdWithAssignments(@Param("id") Long id);

    @Query("""
    SELECT DISTINCT c FROM ClassEntity c
    LEFT JOIN FETCH c.assignments
    LEFT JOIN FETCH c.students s
    LEFT JOIN FETCH s.studentDetails
    LEFT JOIN FETCH c.teacherCourses tc
    LEFT JOIN FETCH tc.teacher
    LEFT JOIN FETCH tc.course
    LEFT JOIN FETCH tc.classes
    WHERE c.id = :id
    """)
    Optional<ClassEntity> findByIdWithAllDetails(@Param("id") Long id);

    @Query("""
    SELECT DISTINCT c FROM ClassEntity c
    LEFT JOIN FETCH c.teacherCourses tc
    LEFT JOIN FETCH tc.teacher t
    LEFT JOIN FETCH tc.course
    LEFT JOIN FETCH tc.classes tcClasses
    LEFT JOIN FETCH c.students
    LEFT JOIN FETCH c.assignments a
    WHERE t.id = :teacherId
    """)
    List<ClassEntity> findClassesByTeacherId(@Param("teacherId") Long teacherId);

    Set<ClassEntity> findAllByIdIn(List<Long> ids);
}
