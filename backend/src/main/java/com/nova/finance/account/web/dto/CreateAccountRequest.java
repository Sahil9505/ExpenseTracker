package com.nova.finance.account.web.dto;

import com.nova.finance.account.Account;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Payload for creating an account. The initial {@code balance} is optional and
 * defaults to zero; it represents the account's starting balance before any
 * tracked transactions.
 */
public record CreateAccountRequest(

        @NotBlank(message = "Account name is required")
        @Size(max = 120, message = "Account name is too long")
        String name,

        @NotNull(message = "Account type is required")
        Account.Type type,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 8, message = "Currency must be 3–8 characters")
        String currency,

        @DecimalMax(value = "99999999999999.9999", message = "Balance is too large")
        BigDecimal balance,

        @Size(max = 120, message = "Institution name is too long")
        String institution,

        @Size(max = 32, message = "Color is too long")
        String color,

        @Size(max = 64, message = "Icon is too long")
        String icon
) {
}
