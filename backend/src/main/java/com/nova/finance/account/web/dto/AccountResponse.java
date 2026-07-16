package com.nova.finance.account.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * API-safe projection of an {@link com.nova.finance.account.Account}. The user is
 * never included; the account is always implicitly scoped to the caller.
 */
public record AccountResponse(
        UUID id,
        String name,
        String type,
        String currency,
        BigDecimal balance,
        boolean active,
        String institution,
        String color,
        String icon,
        Instant createdAt,
        Instant updatedAt
) {
}
