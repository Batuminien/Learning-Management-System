package com.lsm.repository;

import com.lsm.model.entity.PastExam;
import com.lsm.model.entity.StudentExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PastExamRepository extends JpaRepository<PastExam, Long> {

    @Query("SELECT r FROM StudentExamResult r WHERE r.student.id = :studentId")
    List<StudentExamResult> findAllResultsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT e FROM PastExam e WHERE e.examType = :examType")
    List<PastExam> findAllByExamType(@Param("examType") PastExam.ExamType examType);
}
