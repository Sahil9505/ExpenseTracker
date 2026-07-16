package com.nova.auth.security;

import com.nova.common.exception.InvalidTokenException;
import com.nova.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

/**
 * Issues, validates, rotates, and revokes refresh tokens. Raw tokens are
 * generated with a CSPRNG and never persisted; only their SHA-256 hash is stored.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDays;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            com.nova.common.config.JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDays = jwtProperties.refreshTokenDays();
    }

    /** Issues a new raw refresh token (returned to the caller exactly once). */
    @Transactional
    public String issue(User user) {
        String raw = generateRawToken();
        Instant expiresAt = Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS);
        refreshTokenRepository.save(new RefreshToken(user, hash(raw), expiresAt));
        return raw;
    }

    /** Validates a raw token and returns the owning user id, or throws. */
    @Transactional(readOnly = true)
    public UUID validate(String rawToken) {
        RefreshToken token = lookup(rawToken);
        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException();
        }
        return token.getUser().getId();
    }

    /** Rotates a valid raw token: revokes it and returns a brand-new raw token. */
    @Transactional
    public String rotate(String rawToken) {
        RefreshToken old = lookup(rawToken);
        if (old.isRevoked() || old.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException();
        }
        User user = old.getUser();
        String newRaw = generateRawToken();
        RefreshToken next = refreshTokenRepository.save(
                new RefreshToken(user, hash(newRaw), Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS)));

        old.setRevoked(true);
        old.setReplacedBy(next.getId());
        refreshTokenRepository.save(old);
        return newRaw;
    }

    /** Revokes a single refresh token if it is known. */
    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    /** Revokes every active refresh token for a user (logout everywhere). */
    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private RefreshToken lookup(String rawToken) {
        return refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidTokenException::new);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable.", e);
        }
    }
}
