package com.lsm.mapper;

import com.lsm.model.DTOs.DeviceTokenDTO;
import com.lsm.model.entity.DeviceToken;
import org.springframework.stereotype.Component;

@Component
public class DeviceTokenMapper {

    public DeviceTokenDTO toDTO(DeviceToken entity) {
        if (entity == null) return null;

        return DeviceTokenDTO.builder()
                .id(entity.getId())
                .deviceToken(entity.getDeviceToken())
                .deviceType(entity.getDeviceType())
                .deviceName(entity.getDeviceName())
                .osVersion(entity.getOsVersion())
                .appVersion(entity.getAppVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}