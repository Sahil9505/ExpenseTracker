package com.nova.finance.dashboard;

import com.nova.finance.AbstractFinanceApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardApiTest extends AbstractFinanceApiTest {

    private final String password = "sup3rSecret!";

    private String categoryId(String token, String name, String type) throws Exception {
        var list = mockMvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        for (var node : parse(list)) {
            if (name.equals(node.get("name").asText()) && type.equals(node.get("type").asText())) {
                return node.get("id").asText();
            }
        }
        throw new IllegalStateException("category not found: " + name);
    }

    @Test
    void emptySummaryWhenNoData() throws Exception {
        String token = register(email(), password).get("accessToken");
        mockMvc.perform(get("/api/dashboard/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalBalance").value(0))
                .andExpect(jsonPath("$.data.monthlyIncome").value(0))
                .andExpect(jsonPath("$.data.monthlyExpenses").value(0))
                .andExpect(jsonPath("$.data.netCashFlow").value(0))
                .andExpect(jsonPath("$.data.recentTransactions.length()").value(0))
                .andExpect(jsonPath("$.data.monthlyTrend.length()").value(6));
    }

    @Test
    void summaryReflectsRealData() throws Exception {
        String token = register(email(), password).get("accessToken");
        String checking = createAccount(token, "Checking", "CHECKING", "USD", 1000);
        String savings = createAccount(token, "Savings", "SAVINGS", "USD", 500);
        String salary = categoryId(token, "Salary", "INCOME");
        String food = categoryId(token, "Food", "EXPENSE");

        String now = OffsetDateTime.now().toString();
        createTransaction(token, Map.of(
                "amount", 200, "type", "INCOME",
                "accountId", checking, "categoryId", salary, "occurredAt", now));
        createTransaction(token, Map.of(
                "amount", 150, "type", "EXPENSE",
                "accountId", checking, "categoryId", food, "occurredAt", now));

        mockMvc.perform(get("/api/dashboard/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalBalance").value(1550))
                .andExpect(jsonPath("$.data.monthlyIncome").value(200))
                .andExpect(jsonPath("$.data.monthlyExpenses").value(150))
                .andExpect(jsonPath("$.data.netCashFlow").value(50))
                .andExpect(jsonPath("$.data.accountsCount").value(2))
                .andExpect(jsonPath("$.data.activeAccountsCount").value(2))
                .andExpect(jsonPath("$.data.recentTransactions.length()").value(2))
                .andExpect(jsonPath("$.data.categoryBreakdown[?(@.name == 'Food')]").exists())
                .andExpect(jsonPath("$.data.monthlyTrend.length()").value(6))
                .andExpect(jsonPath("$.data.monthlyTrend[5].income").value(200))
                .andExpect(jsonPath("$.data.monthlyTrend[5].expenses").value(150));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));
    }
}
