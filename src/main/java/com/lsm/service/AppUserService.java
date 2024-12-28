package com.lsm.service;

import com.lsm.exception.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lsm.model.entity.base.AppUser;
import com.lsm.repository.AppUserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AppUserService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    @Autowired
    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String loginIdentifier) throws UsernameNotFoundException {
        Optional<AppUser> userOpt = appUserRepository.findByUsernameOrEmail(loginIdentifier, loginIdentifier);
        return userOpt.orElseThrow(() -> new UserNotFoundException("User not found with username or email: " + loginIdentifier));
    }

    @Transactional
    public AppUser findByUsername(String loginIdentifier) {
        Optional<AppUser> userOpt = appUserRepository.findByUsernameOrEmail(loginIdentifier, loginIdentifier);
        return userOpt.orElseThrow(() -> new UserNotFoundException("User not found with username or email: " + loginIdentifier));
    }

    @Transactional(readOnly = true)
    public AppUser getCurrentUserWithDetails(Long userId) {
        return appUserRepository.findUserWithAllDetails(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}