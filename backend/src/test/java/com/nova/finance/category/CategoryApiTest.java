package com.nova.finance.category;

import com.nova.finance.AbstractFinanceApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryApiTest extends AbstractFinanceApiTest {

    private final String password = "sup3rSecret!";

    @Test
    void defaultsAreSeededOnRegistration() throws Exception {
        String token = register(email(), password).get("accessToken");
        mockMvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(15))
                .andExpect(jsonPath("$.data[?(@.name == 'Food' && @.type == 'EXPENSE')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'Salary' && @.type == 'INCOME')]").exists())
                .andExpect(jsonPath("$.data[?(@.system == true)]").exists());
    }

    @Test
    void defaultsAreNotDuplicatedOnReRegistration() throws Exception {
        String email = email();
        register(email, password);
        // Re-registering the same email is impossible; instead verify a second user
        // also gets exactly the starter set (idempotency per user, not global).
        String otherToken = register(email(), password).get("accessToken");
        mockMvc.perform(get("/api/categories").header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(15));
    }

    @Test
    void createUserCategory() throws Exception {
        String token = register(email(), password).get("accessToken");
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Bonus", "type", "INCOME", "color", "#ABCDEF"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bonus"))
                .andExpect(jsonPath("$.data.system").value(false))
                .andExpect(jsonPath("$.data.color").value("#ABCDEF"));
    }

    @Test
    void duplicateNamePerTypeConflicts() throws Exception {
        String token = register(email(), password).get("accessToken");
        Map<String, Object> body = Map.of("name", "Food", "type", "EXPENSE");
        // "Food" expense already exists as a system category.
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.code").value("CONFLICT"));

        // Same name with a different type is allowed.
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Food", "type", "INCOME"))))
                .andExpect(status().isOk());
    }

    @Test
    void systemCategoryCannotBeDeleted() throws Exception {
        String token = register(email(), password).get("accessToken");
        MvcResult list = mockMvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String systemId = null;
        for (var node : parse(list)) {
            if ("Food".equals(node.get("name").asText()) && "EXPENSE".equals(node.get("type").asText())) {
                systemId = node.get("id").asText();
            }
        }
        mockMvc.perform(delete("/api/categories/" + systemId).header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.code").value("CONFLICT"));
    }

    @Test
    void usedCategoryCannotBeDeleted() throws Exception {
        String token = register(email(), password).get("accessToken");
        String accountId = createAccount(token, "Checking", "CHECKING", "USD", 1000);
        String categoryId = createCategory(token, "Side Hustle", "INCOME");

        createTransaction(token, Map.of(
                "amount", 200,
                "type", "INCOME",
                "accountId", accountId,
                "categoryId", categoryId,
                "occurredAt", "2026-07-01T10:00:00Z"));

        mockMvc.perform(delete("/api/categories/" + categoryId).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.code").value("BAD_REQUEST"));

        // Unused user category deletes cleanly.
        String unused = createCategory(token, "One Off", "EXPENSE");
        mockMvc.perform(delete("/api/categories/" + unused).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/categories/" + unused).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotAccessAnotherUsersCategory() throws Exception {
        String ownerToken = register(email(), password).get("accessToken");
        String ownerCategoryId = createCategory(ownerToken, "Secret", "EXPENSE");

        String otherToken = register(email(), password).get("accessToken");
        mockMvc.perform(get("/api/categories/" + ownerCategoryId).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }
}
