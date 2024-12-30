package com.lsm.repository;

import com.lsm.model.entity.AnnouncementReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AnnouncementReadStatusRepository extends JpaRepository<AnnouncementReadStatus, Long> {
    Optional<AnnouncementReadStatus> findByAnnouncementIdAndUserId(Long announcementId, Long userId);
}
