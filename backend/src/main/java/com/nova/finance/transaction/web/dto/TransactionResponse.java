package com.nova.finance.transaction.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * API-safe projection of a {@link com.nova.finance.transaction.Transaction}.
 * Accounts and category are inlined as small references so the UI can render them
 * without extra lookups, and no user entity is ever exposed.
 */
public record TransactionResponse(
        UUID id,
        BigDecimal amount,
        String type,
        AccountRef account,
        AccountRef destinationAccount,
        CategoryRef category,
        String merchant,
        String note,
        String currency,
        String tags,
        OffsetDateTime occurredAt,
        Instant createdAt,
        Instant updatedAt
) {

    public record AccountRef(UUID id, String name, String type, String currency) {
    }

    public record CategoryRef(UUID id, String name, String type, String color, String icon) {
    }
}
