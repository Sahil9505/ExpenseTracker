package com.expensetracker.service;

import com.expensetracker.entity.User;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AuthService authService;

    private User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public List<Map<String, Object>> getMonthlyReports(Integer year) {
        if (year == null) year = LocalDate.now().getYear();
        User user = getCurrentUser();
        return expenseRepository.getMonthlyTotalsByYear(user.getId(), year);
    }

    public List<Map<String, Object>> getCategoryBreakdown(Integer month, Integer year) {
        User user = getCurrentUser();
        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();

        BigDecimal total = expenseRepository.getTotalAmountByUserIdAndMonth(user.getId(), month, year);
        List<Map<String, Object>> categoryTotals = expenseRepository.getCategoryWiseTotalsByMonth(user.getId(), month, year);

        List<Map<String, Object>> breakdown = new ArrayList<>();
        for (Map<String, Object> entry : categoryTotals) {
            Map<String, Object> item = new HashMap<>(entry);
            BigDecimal amount = new BigDecimal(entry.get("total").toString());
            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0 
                    ? amount.multiply(new BigDecimal(100)).divide(total, 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            item.put("percentage", percentage);
            breakdown.add(item);
        }
        return breakdown;
    }

    public List<Map<String, Object>> getTrends() {
        User user = getCurrentUser();
        LocalDate now = LocalDate.now();
        List<Map<String, Object>> trends = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            BigDecimal total = expenseRepository.getTotalAmountByUserIdAndMonth(user.getId(), date.getMonthValue(), date.getYear());
            
            Map<String, Object> trend = new HashMap<>();
            trend.put("month", date.getMonth().name());
            trend.put("year", date.getYear());
            trend.put("total", total);
            trends.add(trend);
        }
        return trends;
    }
}
