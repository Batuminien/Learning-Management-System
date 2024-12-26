package com.lsm.model.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data transfer object for device registration information")
public class DeviceInfoDTO {

    @NotBlank(message = "Device token is required")
    @Schema(description = "Firebase Cloud Messaging (FCM) token for the device",
            example = "dKUf8234jsDFG...")
    private String deviceToken;

    @Schema(description = "Device name/model for reference (optional)",
            example = "iPhone 13 Pro")
    private String deviceName;

    @Schema(description = "Operating system version (optional)",
            example = "iOS 15.5")
    private String osVersion;

    @Schema(description = "App version installed on device (optional)",
            example = "1.2.3")
    private String appVersion;
}