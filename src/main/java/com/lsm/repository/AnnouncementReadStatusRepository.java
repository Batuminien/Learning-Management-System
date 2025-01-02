package com.lsm.repository;

import com.lsm.model.entity.AnnouncementReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AnnouncementReadStatusRepository extends JpaRepository<AnnouncementReadStatus, Long> {
    Optional<AnnouncementReadStatus> findByAnnouncementIdAndUserId(Long announcementId, Long userId);
    @Modifying
    @Query("DELETE FROM AnnouncementReadStatus ars WHERE ars.announcement.id = :announcementId")
    void deleteByAnnouncementId(Long announcementId);
    @Modifying
    void deleteByAnnouncementIdAndUserId(Long announcementId, Long userId);
}
