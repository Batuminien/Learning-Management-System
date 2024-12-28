package com.lsm.model.DTOs;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePhotoResponseDTO {
    private String photoUrl;
    private String filename;
    private String fileType;
    private long fileSize;
    private LocalDateTime uploadTime;
}
