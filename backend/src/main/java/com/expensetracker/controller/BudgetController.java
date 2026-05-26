package com.expensetracker.controller;

import com.expensetracker.dto.ApiResponse;
import com.expensetracker.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBudgets() {
        return ResponseEntity.ok(ApiResponse.success("Budgets fetched successfully", budgetService.getAllBudgets()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addOrUpdateBudget(@RequestBody Map<String, Object> request) {
        try {
            Long categoryId = Long.parseLong(request.get("categoryId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Integer month = request.get("month") != null ? Integer.parseInt(request.get("month").toString()) : LocalDate.now().getMonthValue();
            Integer year = request.get("year") != null ? Integer.parseInt(request.get("year").toString()) : LocalDate.now().getYear();

            return ResponseEntity.ok(ApiResponse.success("Budget saved", budgetService.addOrUpdateBudget(categoryId, amount, month, year)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateBudget(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            // Reusing addOrUpdate since it handles category/month/year logic
            return addOrUpdateBudget(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<?>> getBudgetStatus(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success("Budget status fetched", budgetService.getBudgetStatus(month, year)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBudget(@PathVariable Long id) {
        try {
            budgetService.deleteBudget(id);
            return ResponseEntity.ok(ApiResponse.success("Budget deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
