package com.nova.common.exception;

/**
 * Thrown when a presented access or refresh token cannot be validated
 * (malformed, expired, revoked, or unknown). Surfaced as {@code INVALID_TOKEN}.
 */
public class InvalidTokenException extends NovaException {

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN, "The presented token is invalid or expired.");
    }
}
