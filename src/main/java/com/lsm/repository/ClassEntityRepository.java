package com.lsm.repository;

import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {
    @Query("SELECT c FROM ClassEntity c " +
            "LEFT JOIN FETCH c.assignments " +
            "LEFT JOIN FETCH c.students " +
            "LEFT JOIN FETCH c.courses")
    List<ClassEntity> findAllWithAssociations();

    Optional<ClassEntity> findClassEntityByName(String className);

    Optional<ClassEntity> getClassEntityById(Long id);

    @Query("SELECT c FROM ClassEntity c LEFT JOIN FETCH c.assignments WHERE c.id = :id")
    Optional<ClassEntity> findByIdWithAssignments(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM ClassEntity c " +
            "LEFT JOIN FETCH c.assignments a " +
            "LEFT JOIN FETCH c.students s " +
            "LEFT JOIN FETCH c.courses co " +
            "WHERE c.teacher.id = :teacherId")
    List<ClassEntity> findClassesByTeacherId(@Param("teacherId") Long teacherId);

    // List<ClassEntity> findAllByIdIn(List<Long> ids);
    Set<ClassEntity> findAllByIdIn(List<Long> ids);
}
