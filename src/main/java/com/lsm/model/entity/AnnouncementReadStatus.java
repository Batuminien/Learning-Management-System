package com.lsm.model.entity;

import com.lsm.model.entity.base.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "announcement_read_status")
public class AnnouncementReadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "announcement_read_status_seq")
    @SequenceGenerator(name = "announcement_read_status_seq", sequenceName = "announcement_read_status_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    private LocalDateTime readAt;
}
