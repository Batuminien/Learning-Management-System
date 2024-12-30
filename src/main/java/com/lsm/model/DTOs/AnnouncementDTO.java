package com.lsm.model.DTOs;

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
}
