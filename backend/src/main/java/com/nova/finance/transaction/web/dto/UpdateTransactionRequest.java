package com.nova.finance.transaction.web.dto;

import com.nova.finance.transaction.Transaction;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Partial update for a transaction. Every field is optional; omitted fields keep
 * their current value. On save the service reverses the prior balance effect and
 * applies the new one, so changing the amount, type, or accounts stays consistent.
 */
public record UpdateTransactionRequest(

        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

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
