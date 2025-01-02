package com.lsm.repository;

import com.lsm.model.entity.AssignmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentDocumentRepository extends JpaRepository<AssignmentDocument, Long> {
    @Override
    Optional<AssignmentDocument> findById(Long aLong);
    List<AssignmentDocument> findByUploadTimeBefore(LocalDateTime date);
}
