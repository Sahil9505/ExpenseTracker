package com.expensetracker.controller;

import com.expensetracker.entity.Bill;
import com.expensetracker.dto.ApiResponse;
import com.expensetracker.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBills() {
        return ResponseEntity.ok(ApiResponse.success("Bills fetched successfully", billService.getAllBills()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addBill(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Title is required"));
            }
            if (request.get("amount") == null || request.get("dueDate") == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Amount and due date are required"));
            }
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            LocalDate dueDate = LocalDate.parse((String) request.get("dueDate"));

            Bill bill = billService.addBill(title, amount, dueDate);
            if (request.get("isPaid") != null) {
                bill.setPaid((Boolean) request.get("isPaid"));
                // Note: ideally addBill would take this, but let's keep it simple for now or update service
            }

            return ResponseEntity.ok(ApiResponse.success("Bill added", bill));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateBill(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            LocalDate dueDate = LocalDate.parse((String) request.get("dueDate"));
            Boolean isPaid = (Boolean) request.get("isPaid");

            return ResponseEntity.ok(ApiResponse.success("Bill updated", billService.updateBill(id, title, amount, dueDate, isPaid)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<?>> markAsPaid(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Bill marked as paid", billService.markAsPaid(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBill(@PathVariable Long id) {
        try {
            billService.deleteBill(id);
            return ResponseEntity.ok(ApiResponse.success("Bill deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
