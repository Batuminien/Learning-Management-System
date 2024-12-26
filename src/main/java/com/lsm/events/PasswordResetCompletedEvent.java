package com.lsm.events;

import com.lsm.model.entity.base.AppUser;

public class PasswordResetCompletedEvent extends UserEvent {
    public PasswordResetCompletedEvent(AppUser user) {
        super(user, "PASSWORD_RESET_COMPLETED");
    }
}
