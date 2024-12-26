package com.lsm.service;

import com.lsm.events.PasswordResetRequestedEvent;
import com.lsm.events.PasswordResetCompletedEvent;
import com.lsm.model.entity.base.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventListenerService {
    private final EmailService emailService;

    @EventListener
    @Async
    public void handlePasswordResetRequestedEvent(PasswordResetRequestedEvent event) {
        try {
            AppUser user = event.getUser();
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    event.getResetToken(),
                    user.getName()
            );
        } catch (Exception e) {
            log.error("Error handling password reset request event", e);
        }
    }

    @EventListener
    @Async
    public void handlePasswordResetCompletedEvent(PasswordResetCompletedEvent event) {
        try {
            AppUser user = event.getUser();
            emailService.sendPasswordResetConfirmationEmail(
                    user.getEmail(),
                    user.getName()
            );
        } catch (Exception e) {
            log.error("Error handling password reset completion event", e);
        }
    }
}