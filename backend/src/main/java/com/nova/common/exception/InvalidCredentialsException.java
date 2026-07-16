package com.nova.common.exception;

/**
 * Thrown when authentication fails (wrong email/password or otherwise invalid
 * credentials). Surfaced to clients as an {@code UNAUTHORIZED} response and
 * deliberately does not distinguish whether the email exists.
 */
public class InvalidCredentialsException extends NovaException {

    public InvalidCredentialsException() {
        super(ErrorCode.UNAUTHORIZED, "Invalid email or password.");
    }
}
