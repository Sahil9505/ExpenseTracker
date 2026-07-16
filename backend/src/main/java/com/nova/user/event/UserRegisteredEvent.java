package com.nova.user.event;

import java.util.UUID;

/**
 * Published by the auth service after a new user is persisted. Finance listeners
 * use it to provision user-scoped defaults (such as starter categories) without
 * the auth package depending on the finance domain.
 */
public class UserRegisteredEvent {

    private final UUID userId;

    public UserRegisteredEvent(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
