package com.expensetracker.controller;

import com.expensetracker.dto.ApiResponse;
import com.expensetracker.entity.RecurringExpense;
import com.expensetracker.service.RecurringExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recurring")
public class RecurringController {

    @Autowired
    private RecurringExpenseService recurringService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringExpense>>> getAllRecurring() {
        return ResponseEntity.ok(ApiResponse.success("Recurring expenses fetched", recurringService.getAllRecurring()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addRecurring(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String frequency = (String) request.get("frequency");
            LocalDate nextDueDate = LocalDate.parse((String) request.get("nextDueDate"));
            Long categoryId = Long.parseLong(request.get("categoryId").toString());

            return ResponseEntity.ok(ApiResponse.success("Recurring expense added", 
                    recurringService.addRecurring(name, amount, frequency, nextDueDate, categoryId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateRecurring(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String frequency = (String) request.get("frequency");
            LocalDate nextDueDate = LocalDate.parse((String) request.get("nextDueDate"));
            Long categoryId = request.get("categoryId") != null ? Long.parseLong(request.get("categoryId").toString()) : null;

            return ResponseEntity.ok(ApiResponse.success("Recurring expense updated", 
                    recurringService.updateRecurring(id, name, amount, frequency, nextDueDate, categoryId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteRecurring(@PathVariable Long id) {
        try {
            recurringService.deleteRecurring(id);
            return ResponseEntity.ok(ApiResponse.success("Recurring expense deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
