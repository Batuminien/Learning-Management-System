package com.lsm.events;

import com.lsm.model.entity.base.AppUser;

public class UserRegisteredEvent extends UserEvent {
    public UserRegisteredEvent(AppUser user) {
        super(user, "USER_REGISTERED");
    }
}
