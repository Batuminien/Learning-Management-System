package com.lsm.repository;


import com.lsm.model.entity.Announcement;
import com.lsm.model.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    // Change from findByClassEntityId to findByClassesId
    List<Announcement> findByClassesId(Long classId);

    // You might also want to add these useful methods
    List<Announcement> findByClassesIn(Collection<Set<ClassEntity>> classes);
    List<Announcement> findByClassesIdIn(Collection<Long> classIds);
    List<Announcement> findByCreatedById(Long userId);
}
