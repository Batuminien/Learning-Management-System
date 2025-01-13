package com.lsm.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendNotification(String deviceToken, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("title", title)  // Add data payload
                .putData("body", body)
                .setToken(deviceToken)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setSound("default")
                                .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .setBadge(1)
                                .build())
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent notification to token: {}, messageId: {}",
                    deviceToken.substring(0, 6), response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to token: {}, error: {}",
                    deviceToken.substring(0, 6), e.getMessage());
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                    e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                log.info("Removing invalid token: {}", deviceToken.substring(0, 6));
                // Remove invalid token
                throw e;
            }
        }
    }

    public void sendNotificationToTopic(String topic, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setTopic(topic)
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
    }
}