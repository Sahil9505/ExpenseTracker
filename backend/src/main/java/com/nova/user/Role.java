package com.nova.user;

/**
 * Authorization role assigned to a user. The foundation ships with {@link #USER}
 * (default) and {@link #ADMIN}; further roles can be added without touching the schema.
 */
public enum Role {
    USER,
    ADMIN
}
