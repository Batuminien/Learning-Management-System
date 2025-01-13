package com.lsm.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.lsm.events.*;
import com.lsm.exception.*;
import com.lsm.model.DTOs.TeacherCourseDTO;
import com.lsm.model.DTOs.auth.*;
import com.lsm.model.DTOs.TokenRefreshResult;
import com.lsm.model.entity.*;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.passay.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.security.auth.login.AccountLockedException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days
    private static final int MAX_REFRESH_TOKEN_PER_USER = 5;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOCK_DURATION = 15; // minutes

    private final AppUserRepository appUserRepository;
    private final ClassEntityRepository classEntityRepository;
    private final CourseRepository courseRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final EventPublisher eventPublisher;
    private final EmailService emailService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Transactional
    public AppUser registerUser(RegisterRequestDTO registerRequest) {
        validateRegistrationRequest(registerRequest);

        // Create user with enabled=false
        AppUser newUser = createUserFromRequest(registerRequest);
        newUser.setEnabled(false); // Email not verified yet
        AppUser savedUser = appUserRepository.save(newUser);

        // Generate verification token
        PasswordResetToken verificationToken = PasswordResetToken.builder()
                .token(generateSecureToken())
                .user(savedUser)
                .expiryDate(Instant.now().plusSeconds(24 * 60 * 60))
                .used(false)
                .build();
        passwordResetTokenRepository.save(verificationToken);

        // Send verification email based on role
        if (savedUser.getRole() == Role.ROLE_ADMIN) {
            emailService.sendAdminVerificationEmail(
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    verificationToken.getToken()
            );
        } else {
            /*
            emailService.sendUserVerificationEmail(
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    verificationToken.getToken()
            );
             */
            savedUser.setEnabled(true); // Don't send email for now.
        }

        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser));
        log.info("User registration initiated for: {}", savedUser.getUsername());

        return savedUser;
    }

    @Transactional
    public void verifyAdminAccount(String token) {
        PasswordResetToken verificationToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new InvalidTokenException("Token has already been used");
        }

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenExpiredException("Verification token has expired");
        }

        AppUser admin = verificationToken.getUser();
        admin.setEnabled(true);
        appUserRepository.save(admin);

        verificationToken.setUsed(true);
        passwordResetTokenRepository.save(verificationToken);

        // Send welcome email
        emailService.sendAdminWelcomeEmail(admin.getEmail(), admin.getFullName());
    }

    @Transactional
    public AuthenticationResult authenticate(LoginRequestDTO loginRequest, String clientIp)
            throws AccountLockedException {
        if (loginAttemptService.isBlocked(clientIp)) {
            throw new AccountLockedException("Account is temporarily locked due to too many failed attempts");
        }

        if (!loginRequest.isValid()) {
            throw new AuthenticationException("Either username or email must be provided");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getLoginIdentifier(),
                            loginRequest.getPassword()
                    )
            );

            AppUser user = (AppUser) authentication.getPrincipal();

            if (!user.isEnabled()) {
                throw new AccountDisabledException("Account is disabled");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Clean up any existing refresh tokens before creating a new one
            cleanupExpiredTokens(user);

            // Generate tokens with remember me consideration
            String accessToken = jwtTokenProvider.generateAccessToken(user, loginRequest.isRememberMe());
            RefreshToken refreshToken = createRefreshToken(user.getId(), loginRequest.isRememberMe());

            loginAttemptService.loginSucceeded(clientIp);
            eventPublisher.publishEvent(new UserLoginEvent(user));

            // Return different expiration times based on remember me
            long expiresIn = loginRequest.isRememberMe() ?
                    REFRESH_TOKEN_VALIDITY * 7 : // 7 times longer for remember me
                    REFRESH_TOKEN_VALIDITY;

            return new AuthenticationResult(accessToken, refreshToken.getToken(), user, expiresIn);

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(clientIp);
            log.warn("Authentication failed for user identifier: {}", loginRequest.getLoginIdentifier());
            throw new AuthenticationException("Invalid credentials");
        }
    }

    @Transactional
    public void cleanupExpiredTokens(AppUser user) {
        Instant now = Instant.now();
        refreshTokenRepository.deleteByUserAndExpiryDateBefore(user, now);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId, boolean rememberMe) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        cleanupOldRefreshTokens(user);

        // Calculate expiration based on remember me
        long validity = rememberMe ?
                REFRESH_TOKEN_VALIDITY * 7 : // 7 times longer for remember me
                REFRESH_TOKEN_VALIDITY;

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(generateSecureToken())
                .expiryDate(Instant.now().plusMillis(validity))
                .rememberMe(rememberMe)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public TokenRefreshResult refreshToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .map(this::verifyRefreshToken)
                .map(token -> {
                    AppUser user = token.getUser();
                    String newAccessToken = jwtTokenProvider.generateAccessToken(user, token.isRememberMe());
                    return new TokenRefreshResult(newAccessToken, refreshToken);
                })
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
    }

    @Transactional
    public void logout(String token, String refreshToken) {
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            Optional<AppUser> userOpt = appUserRepository.findByUsername(username);
            AppUser user = userOpt.orElseThrow(() -> new UserNotFoundException("User not found"));

            // Ensure the refresh token is properly deleted
            if (refreshToken != null) {
                refreshTokenRepository.deleteByToken(refreshToken);
                refreshTokenRepository.flush(); // Force immediate deletion
            } else {
                refreshTokenRepository.deleteByUser(user);
                refreshTokenRepository.flush(); // Force immediate deletion
            }

            jwtTokenProvider.invalidateToken(token);
            eventPublisher.publishEvent(new UserLogoutEvent(user));
            SecurityContextHolder.clearContext();

            log.info("User logged out successfully: {}", username);
        } catch (Exception e) {
            log.error("Error during logout", e);
            throw new LogoutException("Error during logout process");
        }
    }

    @Transactional
    public AuthenticationResult authenticateWithGoogle(String googleToken, String clientIp) {
        try {
            NetHttpTransport transport = new NetHttpTransport();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                throw new AuthenticationException("Invalid Google token");
            }

            // Get user info from token
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            if (!payload.getEmailVerified()) {
                throw new AuthenticationException("Google account email not verified");
            }

            // Find existing user
            AppUser user = appUserRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthenticationException(
                            "No user found with email: " + email + ". Please contact admin for registration."));

            // Create authentication token
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user, false);
            RefreshToken refreshToken = createRefreshToken(user.getId(), false);

            return AuthenticationResult.builder()
                    .user(user)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .expiresIn(REFRESH_TOKEN_VALIDITY)
                    .build();

        } catch (Exception e) {
            throw new AuthenticationException("Failed to verify Google token: " + e.getMessage());
        }
    }


    private AppUser createUserFromRequest(RegisterRequestDTO registerRequest) {
        AppUser .AppUserBuilder userBuilder = AppUser.builder()
                .username(registerRequest.getUsername())
                .name(registerRequest.getFirstName())
                .surname(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .schoolLevel(registerRequest.getSchoolLevel())
                .enabled(false)
                .studentDetails(null)
                .teacherDetails(null);

        if (registerRequest instanceof StudentRegisterRequestDTO) {
            StudentDetails studentDetails = getStudentDetails((StudentRegisterRequestDTO) registerRequest);
            userBuilder.studentDetails(studentDetails);
        }

        if (registerRequest instanceof TeacherRegisterRequestDTO) {
            TeacherDetails teacherDetails = getTeacherDetails((TeacherRegisterRequestDTO) registerRequest);
            userBuilder.teacherDetails(teacherDetails);
        }

        return userBuilder.build();
    }

    @Transactional
    public void verifyAccount(String token) {
        PasswordResetToken verificationToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new InvalidTokenException("Token has already been used");
        }

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenExpiredException("Verification token has expired");
        }

        AppUser user = verificationToken.getUser();
        user.setEnabled(true); // Email is now verified
        appUserRepository.save(user);

        verificationToken.setUsed(true);
        passwordResetTokenRepository.save(verificationToken);

        // Send welcome email based on role
        if (user.getRole() == Role.ROLE_ADMIN) {
            emailService.sendAdminWelcomeEmail(user.getEmail(), user.getFullName());
        } else {
            emailService.sendUserWelcomeEmail(user.getEmail(), user.getFullName());
        }
    }

    private StudentDetails getStudentDetails(StudentRegisterRequestDTO registerRequest) {
        StudentDetails studentDetails = new StudentDetails();
        if (appUserRepository.findByStudentDetails_Tc(registerRequest.getTc()))
            throw new IllegalArgumentException("Student with TC: " + registerRequest.getTc() + " already exists");
        studentDetails.setTc(registerRequest.getTc());
        studentDetails.setClassEntity(classEntityRepository.getClassEntityById(registerRequest.getClassEntity())
                .orElseThrow(() -> new EntityNotFoundException("Class not found")));
        studentDetails.setPhone(registerRequest.getPhone());
        studentDetails.setParentPhone(registerRequest.getParentPhone());
        studentDetails.setBirthDate(registerRequest.getBirthDate());
        studentDetails.setParentName(registerRequest.getParentName());
        studentDetails.setRegistrationDate(registerRequest.getRegistrationDate());
        return studentDetails;
    }

    private TeacherDetails getTeacherDetails(TeacherRegisterRequestDTO registerRequest) {
        TeacherDetails teacherDetails = new TeacherDetails();
        if (appUserRepository.findByTeacherDetails_Tc(registerRequest.getTc()))
            throw new IllegalArgumentException("Teacher with TC: " + registerRequest.getTc() + " already exists");
        teacherDetails.setTc(registerRequest.getTc());
        teacherDetails.setPhone(registerRequest.getPhone());
        teacherDetails.setBirthDate(registerRequest.getBirthDate());

        Set<TeacherCourse> teacherCourses = new HashSet<>();

        for (TeacherCourseDTO courseDTO : registerRequest.getTeacherCourses()) {
            Course course = courseRepository.findById(courseDTO.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseDTO.getCourseId()));

            Set<ClassEntity> classes = new HashSet<>(classEntityRepository.findAllByIdIn(courseDTO.getClassIds()));

            TeacherCourse teacherCourse = TeacherCourse.builder()
                    .course(course)
                    .classes(classes)
                    .build();

            teacherCourses.add(teacherCourse);
        }

        teacherDetails.setTeacherCourses(teacherCourses);
        return teacherDetails;
    }

    private Set<ClassEntity> getClasses(List<Long> classIds) {
        return classEntityRepository.findAllByIdIn(classIds);
    }

    private Set<Course> getCourses(List<Long> courseIds) {
        return courseRepository.findAllByIdIn(courseIds);
    }

    private void validateRegistrationRequest(RegisterRequestDTO request) {
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        validatePassword(request.getPassword());

        // Admin-specific validation
        if (request.getRole() == Role.ROLE_ADMIN) {
            if (!request.getEmail().endsWith("@learnovify.com")) {
                throw new InvalidRequestException("Admin email must be a learnovify.com address");
            }
        }
    }

    private void validatePassword(String password) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 30),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()
        ));

        RuleResult result = validator.validate(new PasswordData(password));
        if (!result.isValid()) {
            throw new InvalidPasswordException("Password does not meet security requirements");
        }
    }

    private String generateSecureToken() {
        return UUID.randomUUID() +
                UUID.randomUUID().toString();
    }

    private void cleanupOldRefreshTokens(AppUser user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserOrderByExpiryDateDesc(user);
        if (tokens.size() >= MAX_REFRESH_TOKEN_PER_USER) {
            refreshTokenRepository.deleteAll(tokens.subList(MAX_REFRESH_TOKEN_PER_USER - 1, tokens.size()));
        }
    }

    private RefreshToken verifyRefreshToken(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException("Refresh token expired");
        }
        return token;
    }
}