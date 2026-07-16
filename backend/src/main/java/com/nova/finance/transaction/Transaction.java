package com.nova.finance.transaction;

import com.nova.common.BaseEntity;
import com.nova.finance.account.Account;
import com.nova.finance.category.Category;
import com.nova.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A single financial event: income, expense, or transfer. The {@code amount} is
 * always stored positive; direction is implied by {@link Type}. For transfers the
 * {@code account} is the source and {@code destinationAccount} is the target.
 *
 * <p>Creating, updating, or deleting a transaction keeps the affected account
 * balances in sync through the transaction service, never by editing an account
 * directly.</p>
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction extends BaseEntity {

    /** Direction of the money movement. */
    public enum Type {
        INCOME, EXPENSE, TRANSFER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(length = 255)
    private String merchant;

    @Column(length = 255)
    private String description;

    @Column(length = 8, nullable = false)
    private String currency;

    @Column(length = 255)
    private String tags;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;
}
