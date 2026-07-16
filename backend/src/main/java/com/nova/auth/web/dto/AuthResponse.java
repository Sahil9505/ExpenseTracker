package com.nova.auth.web.dto;

import com.nova.user.web.dto.UserResponse;

/**
 * Tokens issued on register, login, and refresh. The raw refresh token is
 * returned exactly once and should be stored securely by the client.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, String refreshToken,
                                   long expiresInSeconds, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInSeconds, user);
    }
}
