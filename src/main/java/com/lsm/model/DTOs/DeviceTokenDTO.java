package com.lsm.model.DTOs;

import com.lsm.config.ClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object for device token information")
public class DeviceTokenDTO {

    private Long id;
    private String deviceToken;
    private ClientType deviceType;
    private String deviceName;
    private String osVersion;
    private String appVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}