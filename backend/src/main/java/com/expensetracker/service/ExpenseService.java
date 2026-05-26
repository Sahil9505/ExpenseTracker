package com.expensetracker.service;

import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AuthService authService;

    /**
     * Retrieves the currently authenticated user.
     */
    private User getCurrentUser() {
        return authService.getCurrentUser();
    }

    /**
     * Retrieves all expenses for the current user, newest first.
     */
    public List<Expense> getAllExpenses(String category, Integer month, Integer year, String paymentMethod) {
        User user = getCurrentUser();
        Sort sort = Sort.by(Sort.Direction.DESC, "date");
        return expenseRepository.findByFilters(user.getId(), category, month, year, paymentMethod, sort);
    }

    /**
     * Searches expenses by keyword and filters by category.
     */
    public List<Expense> searchExpenses(String keyword, String category, String sortOrder) {
        User user = getCurrentUser();
        Sort sort = "asc".equalsIgnoreCase(sortOrder) ? Sort.by(Sort.Direction.ASC, "date") : Sort.by(Sort.Direction.DESC, "date");
        return expenseRepository.searchExpenses(user.getId(), keyword, category, sort);
    }

    /**
     * Creates a new expense for the current user.
     */
    public Expense addExpense(String title, BigDecimal amount, String category,
                                LocalDate date, String description, String paymentMethod) {
        User user = getCurrentUser();

        Expense expense = new Expense();
        expense.setTitle(title);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(date);
        expense.setDescription(description);
        expense.setPaymentMethod(paymentMethod);
        expense.setUser(user);

        return expenseRepository.save(expense);
    }

    /**
     * Updates an existing expense.
     */
    public Expense updateExpense(Long expenseId, String title, BigDecimal amount,
                                   String category, LocalDate date, String description, String paymentMethod) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own expenses!");
        }

        expense.setTitle(title);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(date);
        expense.setDescription(description);
        expense.setPaymentMethod(paymentMethod);

        return expenseRepository.save(expense);
    }

    /**
     * Deletes an expense by ID.
     */
    public void deleteExpense(Long expenseId) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own expenses!");
        }

        expenseRepository.deleteById(expenseId);
    }

    /**
     * Get summary data for the dashboard.
     */
    public Map<String, Object> getSummary() {
        User user = getCurrentUser();
        LocalDate now = LocalDate.now();

        BigDecimal totalAmount = expenseRepository.getTotalAmountByUserId(user.getId());
        BigDecimal monthlyAmount = expenseRepository.getTotalAmountByUserIdAndMonth(user.getId(), now.getMonthValue(), now.getYear());
        Long totalTransactions = expenseRepository.getTransactionCountByUserId(user.getId());
        List<Map<String, Object>> categoryTotals = expenseRepository.getCategoryWiseTotals(user.getId());

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAllTime", totalAmount);
        summary.put("totalThisMonth", monthlyAmount);
        summary.put("transactionCount", totalTransactions);
        summary.put("categoryTotals", categoryTotals);
        return summary;
    }
}
