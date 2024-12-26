package com.lsm.service;

import com.lsm.exception.InvalidTokenException;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String ROLES_CLAIM = "roles";
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String USER_ID_CLAIM = "uid";
    private static final String EMAIL_CLAIM = "email";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token.expiration:604800000}") // 7 days default
    private long refreshTokenExpiration;

    @Value("${app.jwt.issuer}")
    private String tokenIssuer;

    private Key signingKey;
    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void init() {
        // Generate a secure key from the secret
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(AppUser user, boolean rememberMe) {
        long expiration = rememberMe ?
                accessTokenExpiration * 7 : // 7 times longer for remember me
                accessTokenExpiration;
        return generateToken(user, TokenType.ACCESS, expiration);
    }

    public String generateRefreshToken(AppUser user) {
        return generateToken(user, TokenType.REFRESH, refreshTokenExpiration);
    }

    private String generateToken(AppUser user, TokenType tokenType, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim(ROLES_CLAIM, user.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .claim(USER_ID_CLAIM, user.getId())
                .claim(EMAIL_CLAIM, user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(tokenIssuer)
                .setId(UUID.randomUUID().toString())
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(USER_ID_CLAIM, Long.class));
    }

    public Role getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> {
            String roleString = claims.get(ROLES_CLAIM, String.class);
            return Role.valueOf(roleString);
        });
    }

    public TokenType getTokenType(String token) {
        return getClaimFromToken(token, claims -> {
            String typeString = claims.get(TOKEN_TYPE_CLAIM, String.class);
            return TokenType.valueOf(typeString);
        });
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            if (isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted");
                return false;
            }

            Claims claims = extractAllClaims(token);

            return validateTokenClaims(claims, userDetails) &&
                    validateTokenType(claims) &&
                    validateTokenExpiration(claims) &&
                    validateTokenIssuer(claims);

        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature", e);
            throw new InvalidTokenException("Invalid token signature");
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired", e);
            throw new InvalidTokenException("Token has expired");
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported", e);
            throw new InvalidTokenException("Unsupported token format");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty", e);
            throw new InvalidTokenException("Token claims are empty");
        }
    }

    public void invalidateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenId = claims.getId();
            Date expiration = claims.getExpiration();

            // Store in Redis with expiration
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        getBlacklistKey(tokenId),
                        "blacklisted",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
            }

            log.info("Token invalidated successfully");
        } catch (Exception e) {
            log.error("Error invalidating token", e);
            throw new InvalidTokenException("Could not invalidate token");
        }
    }

    public AppUser getUserFromToken(String token) {
        Claims claims = extractAllClaims(token);

        return AppUser.builder()
                .id(claims.get(USER_ID_CLAIM, Long.class))
                .username(claims.getSubject())
                .email(claims.get(EMAIL_CLAIM, String.class))
                .role(Role.valueOf(claims.get(ROLES_CLAIM, String.class)))
                .build();
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .requireIssuer(tokenIssuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Failed to extract claims from token", e);
            throw new InvalidTokenException("Invalid token");
        }
    }

    private boolean validateTokenClaims(Claims claims, UserDetails userDetails) {
        return claims.getSubject().equals(userDetails.getUsername());
    }

    private boolean validateTokenType(Claims claims) {
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        return TokenType.ACCESS.name().equals(tokenType);
    }

    private boolean validateTokenExpiration(Claims claims) {
        return !claims.getExpiration().before(new Date());
    }

    private boolean validateTokenIssuer(Claims claims) {
        return claims.getIssuer().equals(tokenIssuer);
    }

    private boolean isTokenBlacklisted(String token) {
        try {
            String tokenId = extractAllClaims(token).getId();
            return Boolean.TRUE.equals(
                    redisTemplate.hasKey(getBlacklistKey(tokenId))
            );
        } catch (Exception e) {
            log.error("Error checking token blacklist", e);
            return true; // Fail-safe: consider suspicious tokens as blacklisted
        }
    }

    private String getBlacklistKey(String tokenId) {
        return "token:blacklist:" + tokenId;
    }

    @Getter
    @RequiredArgsConstructor
    public enum TokenType {
        ACCESS("Access Token"),
        REFRESH("Refresh Token");

        private final String description;
    }
}