package com.nova.auth.web.dto;

import jakarta.validation.constraints.Size;

/**
 * Optional body for logout. When {@code refreshToken} is supplied only that
 * session is revoked; when omitted every active session for the user is revoked.
 */
public record LogoutRequest(
        @Size(max = 512) String refreshToken
) {
}
