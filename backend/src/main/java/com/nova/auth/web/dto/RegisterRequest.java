package com.nova.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration payload. {@code fullName} and {@code preferredCurrency} are
 * optional; sensible defaults are applied server-side.
 */
public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @Size(max = 255) String fullName,
        @Size(min = 3, max = 8) String preferredCurrency
) {
}
