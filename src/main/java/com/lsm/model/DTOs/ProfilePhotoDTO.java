package com.lsm.model.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePhotoDTO {
    private String photoUrl;
    private String filename;
    private String fileType;
    private long fileSize;
    private LocalDateTime uploadTime;
}