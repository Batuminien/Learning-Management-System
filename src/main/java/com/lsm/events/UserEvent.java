package com.lsm.events;

import com.lsm.model.entity.base.AppUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public abstract class UserEvent {
    private final AppUser user;
    private final Instant timestamp = Instant.now();
    private final String eventType;
}
