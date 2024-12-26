package com.lsm.events;

import com.lsm.model.entity.base.AppUser;

public class UserLoginEvent extends UserEvent {
    public UserLoginEvent(AppUser user) {
        super(user, "USER_LOGIN");
    }
}
