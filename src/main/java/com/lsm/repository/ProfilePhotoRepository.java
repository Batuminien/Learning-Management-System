package com.lsm.repository;

import com.lsm.model.entity.ProfilePhoto;
import com.lsm.model.entity.base.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilePhotoRepository extends JpaRepository<ProfilePhoto, Long> {
    Optional<ProfilePhoto> findByUser(AppUser user);
    @Modifying
    @Query("DELETE FROM ProfilePhoto p WHERE p.user = :user")
    void deleteProfilePhotoByUser(@Param("user") AppUser user);
}
