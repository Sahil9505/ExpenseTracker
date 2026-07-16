package com.nova.finance.category;

import com.nova.common.BaseEntity;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A categorization label for transactions. Every category belongs to exactly one
 * user and is typed as {@link Type#INCOME} or {@link Type#EXPENSE}. System
 * categories are seeded on registration and cannot be deleted by the user.
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uq_categories_user_name_type", columnNames = {"user_id", "name", "category_type"})
})
@Getter
@Setter
@NoArgsConstructor
public class Category extends BaseEntity {

    /** Whether the category groups income or expense transactions. */
    public enum Type {
        INCOME, EXPENSE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 16)
    private Type type;

    @Column(length = 32)
    private String color;

    @Column(length = 64)
    private String icon;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    public Category(User user, String name, Type type) {
        this.user = user;
        this.name = name;
        this.type = type;
    }
}
