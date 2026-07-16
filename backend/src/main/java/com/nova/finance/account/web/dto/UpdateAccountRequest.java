package com.nova.finance.account.web.dto;

import com.nova.finance.account.Account;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Partial update for an account. Every field is optional; only supplied fields
 * are changed. {@code balance} overwrites the running balance directly, which is
 * intended for reconciliation (e.g. correcting a starting balance). Transaction
 * effects continue to adjust it afterwards.
 */
public record UpdateAccountRequest(

        @Size(max = 120, message = "Account name is too long")
        String name,

        Account.Type type,

        @Size(min = 3, max = 8, message = "Currency must be 3–8 characters")
        String currency,

        @DecimalMax(value = "99999999999999.9999", message = "Balance is too large")
        BigDecimal balance,

        Boolean active,

        @Size(max = 120, message = "Institution name is too long")
        String institution,

        @Size(max = 32, message = "Color is too long")
        String color,

        @Size(max = 64, message = "Icon is too long")
        String icon
) {
}
