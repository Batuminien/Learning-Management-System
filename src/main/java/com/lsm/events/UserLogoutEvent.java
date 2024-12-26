package com.lsm.events;

import com.lsm.model.entity.base.AppUser;

public class UserLogoutEvent extends UserEvent {
    public UserLogoutEvent(AppUser user) {
        super(user, "USER_LOGOUT");
    }
}
