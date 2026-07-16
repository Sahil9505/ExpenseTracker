package com.nova.finance.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.nova.finance.AbstractFinanceApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionApiTest extends AbstractFinanceApiTest {

    private final String password = "sup3rSecret!";

    private String categoryId(String token, String name, String type) throws Exception {
        MvcResult list = mockMvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode node : parse(list)) {
            if (name.equals(node.get("name").asText()) && type.equals(node.get("type").asText())) {
                return node.get("id").asText();
            }
        }
        throw new IllegalStateException("category not found: " + name);
    }

    private BigDecimal balance(String token, String accountId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/accounts/" + accountId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return parse(result).get("balance").decimalValue();
    }

    private void assertBalance(String expected, BigDecimal actual) {
        org.junit.jupiter.api.Assertions.assertEquals(
                0, new BigDecimal(expected).compareTo(actual), "expected balance " + expected + " but was " + actual);
    }

    @Test
    void incomeAddsToBalance() throws Exception {
        String token = register(email(), password).get("accessToken");
        String accountId = createAccount(token, "Checking", "CHECKING", "USD", 1000);
        String salary = categoryId(token, "Salary", "INCOME");

        createTransaction(token, Map.of(
                "amount", 200, "type", "INCOME",
                "accountId", accountId, "categoryId", salary,
                "occurredAt", "2026-07-01T10:00:00Z"));

        assertBalance("1200", balance(token, accountId));
    }

    @Test
    void expenseSubtractsFromBalance() throws Exception {
        String token = register(email(), password).get("accessToken");
        String accountId = createAccount(token, "Checking", "CHECKING", "USD", 1000);
        String food = categoryId(token, "Food", "EXPENSE");

        createTransaction(token, Map.of(
                "amount", 150, "type", "EXPENSE",
                "accountId", accountId, "categoryId", food,
                "occurredAt", "2026-07-01T10:00:00Z"));

        assertBalance("850", balance(token, accountId));
    }

    @Test
    void transferMovesBetweenAccounts() throws Exception {
        String token = register(email(), password).get("accessToken");
        String source = createAccount(token, "Source", "CHECKING", "USD", 1000);
        String dest = createAccount(token, "Dest", "SAVINGS", "USD", 500);

        createTransaction(token, Map.of(
                "amount", 200, "type", "TRANSFER",
                "accountId", source, "destinationAccountId", dest,
                "occurredAt", "2026-07-01T10:00:00Z"));

        assertBalance("800", balance(token, source));
        assertBalance("700", balance(token, dest));
    }

    @Test
    void updateReversesPreviousEffectAndAppliesNew() throws Exception {
        String token = register(email(), password).get("accessToken");
        String accountId = createAccount(token, "Checking", "CHECKING", "USD", 1000);
        String food = categoryId(token, "Food", "EXPENSE");

        String txId = createTransaction(token, Map.of(
                "amount", 150, "type", "EXPENSE",
                "accountId", accountId, "categoryId", food,
                "occurredAt", "2026-07-01T10:00:00Z"));

        // Change the amount 150 -> 250; balance should land at 1000 - 250 = 750.
        mockMvc.perform(patch("/api/transactions/" + txId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("amount", 250))))
                .andExpect(status().isOk());

        assertBalance("750", balance(token, accountId));
    }

    @Test
    void deleteReversesEffect() throws Exception {
        String token = register(email(), password).get("accessToken");
        String accountId = createAccount(token, "Checking", "CHECKING", "USD", 1000);
        String food = categoryId(token, "Food", "EXPENSE");

        String txId = createTransaction(token, Map.of(
                "amount", 150, "type", "EXPENSE",
                "accountId", accountId, "categoryId", food,
                "occurredAt", "2026-07-01T10:00:00Z"));

        mockMvc.perform(delete("/api/transactions/" + txId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertBalance("1000", balance(token, accountId));
    }

    @Test
    void amountMustBePositive() throws Exception {
        String token = register(email(), password).get("accessToken");
        String accountId = createAccount(token, "Checking", "CHECKING", "USD", 1000);
        String food = categoryId(token, "Food", "EXPENSE");
        Map<String, Object> body = Map.of(
                "amount", -50, "type", "EXPENSE", "accountId", accountId, "categoryId", food);

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void expenseRejectsIncomeCategory() throws Exception {
        String token = register(email(), password).get("accessToken");
        String accountId = createAccount(token, "Checking", "CHECKING", "USD", 1000);
        String salary = categoryId(token, "Salary", "INCOME");

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", 50, "type", "EXPENSE",
                                "accountId", accountId, "categoryId", salary,
                                "occurredAt", "2026-07-01T10:00:00Z"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.code").value("BAD_REQUEST"));
    }

    @Test
    void transferRequiresDifferentAccounts() throws Exception {
        String token = register(email(), password).get("accessToken");
        String accountId = createAccount(token, "Checking", "CHECKING", "USD", 1000);

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", 50, "type", "TRANSFER",
                                "accountId", accountId, "destinationAccountId", accountId,
                                "occurredAt", "2026-07-01T10:00:00Z"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferMustNotCarryCategory() throws Exception {
        String token = register(email(), password).get("accessToken");
        String source = createAccount(token, "Source", "CHECKING", "USD", 1000);
        String dest = createAccount(token, "Dest", "SAVINGS", "USD", 500);
        String food = categoryId(token, "Food", "EXPENSE");

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", 50, "type", "TRANSFER",
                                "accountId", source, "destinationAccountId", dest,
                                "categoryId", food,
                                "occurredAt", "2026-07-01T10:00:00Z"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userCannotUseAnotherUsersAccount() throws Exception {
        String ownerToken = register(email(), password).get("accessToken");
        String ownerAccountId = createAccount(ownerToken, "Private", "CHECKING", "USD", 1000);
        String ownerFood = categoryId(ownerToken, "Food", "EXPENSE");

        String otherToken = register(email(), password).get("accessToken");
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", 50, "type", "EXPENSE",
                                "accountId", ownerAccountId, "categoryId", ownerFood,
                                "occurredAt", "2026-07-01T10:00:00Z"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotAccessAnotherUsersTransaction() throws Exception {
        String ownerToken = register(email(), password).get("accessToken");
        String accountId = createAccount(ownerToken, "Checking", "CHECKING", "USD", 1000);
        String food = categoryId(ownerToken, "Food", "EXPENSE");
        String txId = createTransaction(ownerToken, Map.of(
                "amount", 50, "type", "EXPENSE", "accountId", accountId, "categoryId", food,
                "occurredAt", "2026-07-01T10:00:00Z"));

        String otherToken = register(email(), password).get("accessToken");
        mockMvc.perform(get("/api/transactions/" + txId).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }
}
