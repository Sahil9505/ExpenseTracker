package com.expensetracker.controller;

import com.expensetracker.entity.Expense;
import com.expensetracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.expensetracker.dto.ApiResponse;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    /**
     * Fetch all expenses for the logged-in user with optional filters.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Expense>>> getAllExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String paymentMethod) {
        return ResponseEntity.ok(ApiResponse.success("Expenses fetched successfully", 
                expenseService.getAllExpenses(category, month, year, paymentMethod)));
    }

    /**
     * Search expenses by keyword, filter by category, and sort by date.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Expense>>> searchExpenses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "desc") String sort) {
        
        List<Expense> results = expenseService.searchExpenses(keyword, category, sort);
        return ResponseEntity.ok(ApiResponse.success("Search results fetched successfully", results));
    }

    /**
     * Add a new expense.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> addExpense(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String category = (String) request.get("category");
            String description = (String) request.get("description");
            String paymentMethod = (String) request.get("paymentMethod");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Title is required."));
            }
            if (request.get("amount") == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Amount is required."));
            }

            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Amount must be greater than zero."));
            }

            LocalDate date = request.get("date") != null ? LocalDate.parse((String) request.get("date")) : LocalDate.now();

            Expense expense = expenseService.addExpense(title, amount, category, date, description, paymentMethod);
            return ResponseEntity.ok(ApiResponse.success("Expense added successfully", expense));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update an existing expense.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateExpense(@PathVariable Long id,
                                            @RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String category = (String) request.get("category");
            String description = (String) request.get("description");
            String paymentMethod = (String) request.get("paymentMethod");

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Title is required."));
            }
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Amount must be greater than zero."));
            }

            LocalDate date = request.get("date") != null ? LocalDate.parse((String) request.get("date")) : LocalDate.now();

            Expense expense = expenseService.updateExpense(id, title, amount, category, date, description, paymentMethod);
            return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", expense));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete an expense by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteExpense(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get summary data for the dashboard (total spent and category breakdown).
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<?>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success("Summary fetched successfully", expenseService.getSummary()));
    }
}
