package com.nova.finance.transaction;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Filter criteria for listing transactions. All fields are optional; a null field
 * means "no constraint on this dimension". {@code accountId} matches either the
 * source or the destination account of a transfer.
 */
public record TransactionFilter(
        Transaction.Type type,
        UUID accountId,
        UUID categoryId,
        OffsetDateTime from,
        OffsetDateTime to,
        String search
) {
}
