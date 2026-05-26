package com.expensetracker.service;

import com.expensetracker.entity.ExpenseCategory;
import com.expensetracker.entity.User;
import com.expensetracker.repository.ExpenseCategoryRepository;
import com.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseCategoryService {

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    @Autowired
    private AuthService authService;

    private User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public List<ExpenseCategory> getAllCategories() {
        return categoryRepository.findByUserId(getCurrentUser().getId());
    }

    public ExpenseCategory addCategory(String name, String icon, String color) {
        ExpenseCategory category = new ExpenseCategory();
        category.setName(name);
        category.setIcon(icon);
        category.setColor(color);
        category.setUser(getCurrentUser());
        return categoryRepository.save(category);
    }

    public ExpenseCategory updateCategory(Long id, String name, String icon, String color) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (!category.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Unauthorized");
        }

        category.setName(name);
        category.setIcon(icon);
        category.setColor(color);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        ExpenseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (!category.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Unauthorized");
        }
        categoryRepository.deleteById(id);
    }
}
