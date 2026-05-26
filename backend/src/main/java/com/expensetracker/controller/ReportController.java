package com.expensetracker.controller;

import com.expensetracker.dto.ApiResponse;
import com.expensetracker.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<?>> getMonthlyReports(@RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success("Monthly report fetched", reportService.getMonthlyReports(year)));
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<?>> getCategoryBreakdown(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success("Category breakdown fetched", reportService.getCategoryBreakdown(month, year)));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<?>> getTrends() {
        return ResponseEntity.ok(ApiResponse.success("Spending trends fetched", reportService.getTrends()));
    }
}
