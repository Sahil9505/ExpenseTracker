package com.nova.finance.dashboard;

import com.nova.finance.transaction.web.dto.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated snapshot of a user's finances for the dashboard. Every figure is
 * derived from the user's own accounts and transactions — no other user's data is
 * ever included.
 */
public record DashboardSummaryResponse(
        BigDecimal totalBalance,
        BigDecimal monthlyIncome,
        BigDecimal monthlyExpenses,
        BigDecimal netCashFlow,
        String currency,
        int accountsCount,
        int activeAccountsCount,
        List<TransactionResponse> recentTransactions,
        List<CategoryBreakdownItem> categoryBreakdown,
        List<MonthlyPoint> monthlyTrend
) {

    /** Expense totals grouped by category for the current month. */
    public record CategoryBreakdownItem(String name, String color, String icon, BigDecimal amount) {
    }

    /** One month of income vs. expense, oldest first, for the spending-trend chart. */
    public record MonthlyPoint(String month, String label, BigDecimal income, BigDecimal expenses) {
    }
}
