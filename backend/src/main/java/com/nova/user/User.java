package com.nova.user;

import com.nova.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "preferred_currency", length = 8, nullable = false)
    private String preferredCurrency;

    @Column(name = "timezone", length = 64)
    private String timezone;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /**
     * Creates a newly registered, active user. The password must already be hashed
     * by the caller (BCrypt).
     */
    public User(String email, String fullName, String passwordHash, Role role,
                AccountStatus accountStatus, String preferredCurrency) {
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.accountStatus = accountStatus;
        this.preferredCurrency = preferredCurrency;
    }
}
