package com.lsm.repository;

import com.lsm.model.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lsm.model.entity.base.AppUser;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("""
    SELECT DISTINCT dt.deviceToken 
    FROM DeviceToken dt 
    JOIN dt.user u 
    WHERE u.id = :userId
    """)
    Optional<String> findDeviceTokenByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT DISTINCT u FROM AppUser u
    JOIN u.teacherDetails td
    LEFT JOIN FETCH td.teacherCourses tc
    LEFT JOIN FETCH tc.course c
    LEFT JOIN FETCH tc.classes cls
    WHERE u.id = :userId AND u.role = 'ROLE_TEACHER'
    """)
    Optional<AppUser> findUserWithTeacherDetailsAndClasses(@Param("userId") Long userId);

    @Query("""
    SELECT DISTINCT u FROM AppUser u
    LEFT JOIN FETCH u.teacherDetails td
    LEFT JOIN FETCH td.teacherCourses tc
    LEFT JOIN FETCH tc.course c
    LEFT JOIN FETCH tc.classes
    LEFT JOIN FETCH u.studentDetails sd
    LEFT JOIN FETCH sd.classEntity
    WHERE u.id = :userId
    """)
    Optional<AppUser> findUserWithAllDetails(@Param("userId") Long userId);

    Page<AppUser> findAllByRole(Role role, Pageable pageable);

    boolean findByStudentDetails_Tc(String studentDetailsTc);

    boolean findByTeacherDetails_Tc(String teacherDetailsTc);
}
