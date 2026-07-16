package com.nova.finance.category.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * API-safe projection of a {@link com.nova.finance.category.Category}. System
 * categories are flagged so the UI can prevent deletion and surface a badge.
 */
public record CategoryResponse(
        UUID id,
        String name,
        String type,
        String color,
        String icon,
        boolean system,
        Instant createdAt,
        Instant updatedAt
) {
}
