package com.expensetracker.service;

import com.expensetracker.entity.Budget;
import com.expensetracker.entity.User;
import com.expensetracker.entity.ExpenseCategory;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.ExpenseCategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    @Autowired
    private AuthService authService;

    private User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findByUserId(getCurrentUser().getId());
    }

    public Budget addOrUpdateBudget(Long categoryId, BigDecimal amount, Integer month, Integer year) {
        User user = getCurrentUser();
        ExpenseCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Optional<Budget> existing = budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(user.getId(), categoryId, month, year);
        
        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setAmount(amount);
        } else {
            budget = new Budget();
            budget.setCategory(category);
            budget.setAmount(amount);
            budget.setMonth(month);
            budget.setYear(year);
            budget.setUser(user);
        }
        return budgetRepository.save(budget);
    }

    public List<Map<String, Object>> getBudgetStatus(Integer month, Integer year) {
        User user = getCurrentUser();
        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();

        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(user.getId(), month, year);
        List<Map<String, Object>> categoryWiseTotals = expenseRepository.getCategoryWiseTotalsByMonth(user.getId(), month, year);

        List<Map<String, Object>> statusList = new ArrayList<>();

        for (Budget budget : budgets) {
            Map<String, Object> status = new HashMap<>();
            status.put("category", budget.getCategory().getName());
            status.put("categoryId", budget.getCategory().getId());
            status.put("limit", budget.getAmount());
            
            BigDecimal spent = BigDecimal.ZERO;
            for (Map<String, Object> catTotal : categoryWiseTotals) {
                if (catTotal.get("category").equals(budget.getCategory().getName())) {
                    spent = new BigDecimal(catTotal.get("total").toString());
                    break;
                }
            }
            status.put("spent", spent);
            status.put("remaining", budget.getAmount().subtract(spent));
            status.put("percentage", budget.getAmount().compareTo(BigDecimal.ZERO) > 0 
                    ? spent.multiply(new BigDecimal(100)).divide(budget.getAmount(), 2, java.math.RoundingMode.HALF_UP) 
                    : BigDecimal.ZERO);
            
            statusList.add(status);
        }

        return statusList;
    }

    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id).orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!budget.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Unauthorized");
        }
        budgetRepository.deleteById(id);
    }
}
