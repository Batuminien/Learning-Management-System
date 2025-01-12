package com.lsm.service;

import com.lsm.events.EventPublisher;
import com.lsm.exception.*;
import com.lsm.model.DTOs.auth.*;
import com.lsm.model.entity.RefreshToken;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AppUserRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.repository.CourseRepository;
import com.lsm.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.security.auth.login.AccountLockedException;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
/*
    @Mock private AppUserRepository appUserRepository;
    @Mock private ClassEntityRepository classEntityRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private EventPublisher eventPublisher;

    private AuthService authService;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                appUserRepository,
                classEntityRepository,
                courseRepository,
                refreshTokenRepository,
                passwordEncoder,
                authenticationManager,
                jwtTokenProvider,
                loginAttemptService,
                eventPublisher
        );
    }

    @Test
    void registerUser_WithValidRequest_ShouldSucceed() {
        // Arrange
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password("Test123!@#")
                .role(Role.ROLE_STUDENT)
                .build();

        when(appUserRepository.existsByUsername(any())).thenReturn(false);
        when(appUserRepository.existsByEmail(any())).thenReturn(false);
        when(appUserRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        AppUser result = authService.registerUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.getUsername(), result.getUsername());
        assertEquals(request.getEmail(), result.getEmail());
        assertTrue(passwordEncoder.matches(request.getPassword(), result.getPassword()));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void registerUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("existing")
                .email("test@example.com")
                .password("Test123!@#")
                .build();

        when(appUserRepository.existsByUsername("existing")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> authService.registerUser(request));
    }

    @Test
    void authenticate_WithValidCredentials_ShouldSucceed() throws AccountLockedException {
        // Arrange
        String clientIp = "127.0.0.1";
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "", "password123", false);
        AppUser user = AppUser.builder()
                .id(1L)
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_STUDENT)
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);

        when(loginAttemptService.isBlocked(clientIp)).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access-token");
        // Add this mock to fix the UserNotFoundException
        when(appUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        AuthenticationResult result = authService.authenticate(loginRequest, clientIp);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        verify(loginAttemptService).loginSucceeded(clientIp);
        verify(eventPublisher).publishEvent(any());
        // Add verification for user lookup
        verify(appUserRepository).findById(user.getId());
    }

    @Test
    void authenticate_WhenAccountLocked_ShouldThrowException() {
        // Arrange
        String clientIp = "127.0.0.1";
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "", "password123", false);

        when(loginAttemptService.isBlocked(clientIp)).thenReturn(true);

        // Act & Assert
        assertThrows(AccountLockedException.class,
                () -> authService.authenticate(loginRequest, clientIp));
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldThrowException() {
        // Arrange
        String clientIp = "127.0.0.1";
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "", "wrongpass", false);

        when(loginAttemptService.isBlocked(clientIp)).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(AuthenticationException.class,
                () -> authService.authenticate(loginRequest, clientIp));
        verify(loginAttemptService).loginFailed(clientIp);
    }

    @Test
    void refreshToken_WithValidToken_ShouldSucceed() {
        // Arrange
        String refreshTokenStr = "valid-refresh-token";
        AppUser user = AppUser.builder()
                .id(1L)
                .username("testuser")
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token(refreshTokenStr)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByToken(refreshTokenStr))
                .thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.generateAccessToken(user))
                .thenReturn("new-access-token");

        // Act
        var result = authService.refreshToken(refreshTokenStr);

        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals(refreshTokenStr, result.getRefreshToken());
    }

    @Test
    void logout_ShouldInvalidateTokensAndClearContext() {
        // Arrange
        String accessToken = "valid-access-token";
        String refreshToken = "valid-refresh-token";
        String username = "testuser";
        AppUser user = AppUser.builder()
                .id(1L)
                .username(username)
                .build();

        when(jwtTokenProvider.getUsernameFromToken(accessToken)).thenReturn(username);
        when(appUserRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        authService.logout(accessToken, refreshToken);

        // Assert
        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verify(jwtTokenProvider).invalidateToken(accessToken);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void createRefreshToken_ShouldCreateNewToken() {
        // Arrange
        Long userId = 1L;
        AppUser user = AppUser.builder()
                .id(userId)
                .username("testuser")
                .build();

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        RefreshToken result = authService.createRefreshToken(userId);

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertNotNull(result.getToken());
        assertNotNull(result.getExpiryDate());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
    }

 */
}