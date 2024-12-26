package com.lsm.model.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AnnouncementDTO {
    private Long id;
    @NotNull private String title;
    @NotNull private String content;
    @NotNull private List<Long> classIds;
    private LocalDate createdAt;
}
