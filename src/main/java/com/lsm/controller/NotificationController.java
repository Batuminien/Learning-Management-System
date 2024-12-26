package com.lsm.controller;

import com.lsm.config.ClientType;
import com.lsm.config.ClientTypeInterceptor;
import com.lsm.mapper.DeviceTokenMapper;
import com.lsm.model.DTOs.DeviceInfoDTO;
import com.lsm.model.DTOs.DeviceTokenDTO;
import com.lsm.model.DTOs.DeviceTokenRequestDTO;
import com.lsm.model.entity.DeviceToken;
import com.lsm.model.entity.base.AppUser;
import com.lsm.repository.DeviceTokenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final DeviceTokenRepository deviceTokenRepository;
    private final DeviceTokenMapper deviceTokenMapper;

    @PostMapping("/register-token")
    public ResponseEntity<ApiResponse_<DeviceTokenDTO>> registerToken(
            @Valid @RequestBody DeviceTokenRequestDTO request,
            Authentication authentication) {
        AppUser user = (AppUser) authentication.getPrincipal();

        DeviceToken deviceToken = DeviceToken.builder()
                .user(user)
                .deviceToken(request.getDeviceToken())
                .deviceType(request.getDeviceType())
                .build();

        DeviceToken savedToken = deviceTokenRepository.save(deviceToken);
        return ResponseEntity.ok(
                new ApiResponse_<>(
                        true,
                        "Device token registered successfully",
                        deviceTokenMapper.toDTO(savedToken)
                )
        );
    }

    @PostMapping("/register-device")
    public ResponseEntity<ApiResponse_<DeviceTokenDTO>> registerDevice(
            @Valid @RequestBody DeviceInfoDTO deviceInfo,
            Authentication authentication) {
        try {
            AppUser user = (AppUser) authentication.getPrincipal();
            ClientType clientType = ClientTypeInterceptor.getCurrentClientType();

            // Only allow registration for mobile clients
            if (clientType == ClientType.WEB) {
                return ApiResponse_.httpError(
                        HttpStatus.BAD_REQUEST,
                        "Web clients cannot register for push notifications"
                );
            }

            DeviceToken deviceToken = DeviceToken.builder()
                    .user(user)
                    .deviceToken(deviceInfo.getDeviceToken())
                    .deviceType(clientType)
                    .deviceName(deviceInfo.getDeviceName())
                    .osVersion(deviceInfo.getOsVersion())
                    .appVersion(deviceInfo.getAppVersion())
                    .build();

            DeviceToken savedToken = deviceTokenRepository.save(deviceToken);

            return ResponseEntity.ok(new ApiResponse_<>(
                    true,
                    "Device registered successfully",
                    deviceTokenMapper.toDTO(savedToken)
            ));
        } catch (Exception e) {
            log.error("Error registering device: {}", e.getMessage());
            return ApiResponse_.httpError(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to register device: " + e.getMessage()
            );
        }
    }

    @DeleteMapping("/unregister-token")
    public ResponseEntity<ApiResponse_<Void>> unregisterToken(@RequestParam String deviceToken) {
        deviceTokenRepository.deleteByDeviceToken(deviceToken);
        return ResponseEntity.ok(
                new ApiResponse_<>(
                        true,
                        "Device token unregistered successfully",
                        null
                )
        );
    }
}