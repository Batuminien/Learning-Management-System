package com.lsm.events;

import com.lsm.model.entity.base.AppUser;
import lombok.Getter;

@Getter
public class PasswordResetRequestedEvent extends UserEvent {
    private final String resetToken;

    public PasswordResetRequestedEvent(AppUser user, String resetToken) {
        super(user, "PASSWORD_RESET_REQUESTED");
        this.resetToken = resetToken;
    }
}
