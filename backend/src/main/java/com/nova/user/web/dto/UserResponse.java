package com.nova.user.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Safe projection of a {@code User} for API responses. The password hash is
 * intentionally never included.
 */
public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String role,
        String accountStatus,
        String preferredCurrency,
        String timezone,
        String avatarUrl,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt
) {
}
