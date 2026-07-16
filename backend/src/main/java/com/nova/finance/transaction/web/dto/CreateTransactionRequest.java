package com.nova.finance.transaction.web.dto;

import com.nova.finance.transaction.Transaction;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Payload for recording a transaction. Rules by type are enforced in the service:
 * <ul>
 *   <li>INCOME / EXPENSE require {@code accountId} and {@code categoryId}; the
 *       category type must match.</li>
 *   <li>TRANSFER requires {@code accountId} (source) and {@code destinationAccountId};
 *       they must differ, and no category is used.</li>
 * </ul>
 * The {@code amount} is always positive; direction comes from {@code type}.
 */
public record CreateTransactionRequest(

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Transaction type is required")
        Transaction.Type type,

        UUID accountId,

        UUID destinationAccountId,

        UUID categoryId,

        @Size(max = 255, message = "Merchant is too long")
        String merchant,

        @Size(max = 255, message = "Note is too long")
        String note,

        @Size(min = 3, max = 8, message = "Currency must be 3–8 characters")
        String currency,

        @Size(max = 255, message = "Tags are too long")
        String tags,

        OffsetDateTime occurredAt
) {
}
