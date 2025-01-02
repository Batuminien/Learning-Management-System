package com.lsm.model.DTOs;

import com.lsm.model.entity.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AnnouncementDTO {
    private Long id;
    @NotNull private String title;
    @NotNull private String content;
    @NotNull private List<Long> classIds;
    private LocalDateTime createdAt;
    private boolean isRead;
    private LocalDateTime readAt;
    private Long createdById;
    private String createdByName;
    @Enumerated(EnumType.STRING) private Role creatorRole;
}
