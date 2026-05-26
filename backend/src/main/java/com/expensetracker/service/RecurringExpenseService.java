package com.expensetracker.service;

import com.expensetracker.entity.ExpenseCategory;
import com.expensetracker.entity.RecurringExpense;
import com.expensetracker.entity.User;
import com.expensetracker.repository.ExpenseCategoryRepository;
import com.expensetracker.repository.RecurringExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringExpenseService {

    @Autowired
    private RecurringExpenseRepository recurringRepository;

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    @Autowired
    private AuthService authService;

    private User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public List<RecurringExpense> getAllRecurring() {
        return recurringRepository.findByUser(getCurrentUser());
    }

    public RecurringExpense addRecurring(String name, BigDecimal amount, String frequency, LocalDate nextDueDate, Long categoryId) {
        User user = getCurrentUser();
        ExpenseCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        RecurringExpense recurring = new RecurringExpense();
        recurring.setName(name);
        recurring.setAmount(amount);
        recurring.setFrequency(frequency);
        recurring.setNextDueDate(nextDueDate);
        recurring.setCategory(category);
        recurring.setUser(user);

        return recurringRepository.save(recurring);
    }

    public RecurringExpense updateRecurring(Long id, String name, BigDecimal amount, String frequency, LocalDate nextDueDate, Long categoryId) {
        User user = getCurrentUser();
        RecurringExpense recurring = recurringRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring expense not found"));

        if (!recurring.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (categoryId != null) {
            ExpenseCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            recurring.setCategory(category);
        }

        recurring.setName(name);
        recurring.setAmount(amount);
        recurring.setFrequency(frequency);
        recurring.setNextDueDate(nextDueDate);

        return recurringRepository.save(recurring);
    }

    public void deleteRecurring(Long id) {
        RecurringExpense recurring = recurringRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring expense not found"));

        if (!recurring.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Unauthorized");
        }
        recurringRepository.deleteById(id);
    }
}
