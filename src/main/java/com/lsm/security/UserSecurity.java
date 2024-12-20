package com.lsm.security;

import com.lsm.model.entity.base.AppUser;
import com.lsm.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {
    private final JwtTokenProvider jwtTokenProvider;

    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        AppUser currentUser = (AppUser) authentication.getPrincipal();
        return currentUser.getId().equals(userId);
    }
}
