package com.lsm.service;

import com.lsm.events.EventPublisher;
import com.lsm.events.PasswordResetRequestedEvent;
import com.lsm.events.PasswordResetCompletedEvent;
import com.lsm.exception.*;
import com.lsm.model.DTOs.auth.*;
import com.lsm.model.entity.*;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.validation.PasswordConstraintValidator;
import com.lsm.repository.AppUserRepository;
import com.lsm.repository.PasswordResetTokenRepository;
import com.lsm.security.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.dependencycheck.utils.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    private static final int MAX_RESET_ATTEMPTS = 3;
    private static final long RATE_LIMIT_DURATION = 24 * 60 * 60 * 1000; // 24 hours

    @Value("${app.password-reset.token.expiration:3600000}") // 1 hour default
    private long tokenValidityPeriod;

    private final AppUserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    private final RateLimiter rateLimiterService;

    @Transactional
    public void requestPasswordReset(PasswordResetRequestDTO request, String clientIp)
            throws TooManyRequestsException {
        String email = request.getEmail().toLowerCase().trim();

        // Rate limiting check using your implementation
        try {
            rateLimiterService.checkRateLimit("password_reset:" + email);
        } catch (RateLimitExceededException e) {
            throw new TooManyRequestsException(e.getMessage());
        }

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Invalidate any existing tokens
        tokenRepository.findByUserEmail(email)
                .ifPresent(token -> {
                    token.setUsed(true);
                    tokenRepository.save(token);
                });

        // Create new token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(generateSecureToken())
                .user(user)
                .expiryDate(Instant.now().plusMillis(tokenValidityPeriod))
                .used(false)
                .createdAt(Instant.now())
                .build();

        tokenRepository.save(resetToken);

        // Publish event for email notification
        eventPublisher.publishEvent(new PasswordResetRequestedEvent(user, resetToken.getToken()));

        log.info("Password reset requested for user: {}", email);
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidPasswordException("Passwords do not match");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        validateResetToken(resetToken);

        AppUser user = resetToken.getUser();
        validateNewPassword(request.getNewPassword(), user);

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Publish event for notification
        eventPublisher.publishEvent(new PasswordResetCompletedEvent(user));

        log.info("Password reset completed for user: {}", user.getEmail());
    }

    private void validateResetToken(PasswordResetToken token) {
        if (token.isUsed()) {
            throw new InvalidTokenException("Token has already been used");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenExpiredException("Token has expired");
        }

        if (!token.getUser().isEnabled()) {
            throw new AccountDisabledException("Account is disabled");
        }
    }

    private void validateNewPassword(String newPassword, AppUser user) {
        // Validate password requirements (reuse your existing password validation logic)
        if (validatePassword(newPassword))
            throw new InvalidPasswordException("Passwords is not valid.");

        // Ensure new password is different from the current one
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new InvalidPasswordException("New password must be different from the current password");
        }
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString() +
                UUID.randomUUID().toString();
    }

    private boolean validatePassword(String password) {
        PasswordConstraintValidator passwordConstraintValidator = new PasswordConstraintValidator();
        return passwordConstraintValidator.isValid(password, null);
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(Instant.now());
    }
}