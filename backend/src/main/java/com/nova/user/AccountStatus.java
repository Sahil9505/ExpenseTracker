package com.nova.user;

/**
 * Lifecycle state of a user account. Drives Spring Security's enabled/locked
 * checks so a disabled or locked account cannot authenticate with a valid token.
 */
public enum AccountStatus {
    ACTIVE,
    DISABLED,
    LOCKED,
    PENDING
}
