package com.lsm.model.DTOs;

import com.lsm.config.ClientType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceTokenRequestDTO {
    @NotBlank(message = "Device token is required")
    private String deviceToken;
    private ClientType deviceType;
}