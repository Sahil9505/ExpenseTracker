package com.expensetracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Expense Entity - Maps to the 'expenses' table in MySQL.
 * Each expense belongs to a specific user.
 */
@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // e.g., "Lunch at McDonald's"

    @Column(nullable = false)
    private BigDecimal amount; // e.g., 250.00

    @Column(nullable = false)
    private String category; // Food, Travel, Shopping, Bills, Other

    @Column(nullable = false)
    private LocalDate date; // Date of the expense

    private String description; // Optional note about the expense

    private String paymentMethod; // Cash, Card, UPI, etc.

    /**
     * Many expenses can belong to one user.
     * FetchType.LAZY means user data won't be loaded unless explicitly needed.
     * JoinColumn creates a foreign key 'user_id' in the expenses table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
