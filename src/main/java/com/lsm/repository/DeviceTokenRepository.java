package com.lsm.repository;

import com.lsm.model.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByUserId(Long userId);
    void deleteByDeviceToken(String deviceToken);
    @Query("SELECT DISTINCT dt.deviceToken FROM DeviceToken dt " +
            "JOIN dt.user u " +
            "WHERE (u.studentDetails.classEntity = :classId) OR " +
            "(u.role = 'ROLE_TEACHER' AND EXISTS (" +
            "SELECT 1 FROM u.teacherDetails.classes c " +
            "WHERE c.id = :classId" +
            "))")
    List<String> findTokensByClassId(@Param("classId") Long classId);
}
