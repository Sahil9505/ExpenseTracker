package com.nova.user.web.dto;

import jakarta.validation.constraints.Size;

/**
 * Partial profile update (PATCH). Every field is optional; {@code null} leaves
 * the existing value unchanged.
 */
public record UpdateProfileRequest(
        @Size(max = 255) String fullName,
        @Size(min = 3, max = 8) String preferredCurrency,
        @Size(max = 64) String timezone,
        @Size(max = 512) String avatarUrl
) {
}
