package com.lsm.model.DTOs;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePhotoUpdateRequestDTO {
    private String photoUrl;
    private String filename;
}
