package com.nova.finance.account;

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

class AccountApiTest extends AbstractFinanceApiTest {

    private final String password = "sup3rSecret!";

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));
    }

    @Test
    void createListAndFetch() throws Exception {
        String token = register(email(), password).get("accessToken");

        MvcResult created = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Everyday", "type", "CHECKING", "currency", "USD", "balance", 1200))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Everyday"))
                .andExpect(jsonPath("$.data.type").value("CHECKING"))
                .andExpect(jsonPath("$.data.balance").value(1200))
                .andExpect(jsonPath("$.data.active").value(true))
                .andReturn();
        String id = parse(created).get("id").asText();

        mockMvc.perform(get("/api/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/accounts/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    void currencyIsNormalizedToUppercase() throws Exception {
        String token = register(email(), password).get("accessToken");
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Euro", "type", "SAVINGS", "currency", "eur", "balance", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currency").value("EUR"));
    }

    @Test
    void invalidCurrencyIsRejected() throws Exception {
        String token = register(email(), password).get("accessToken");
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Bad", "type", "CASH", "currency", "US", "balance", 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void duplicateActiveNameConflicts() throws Exception {
        String token = register(email(), password).get("accessToken");
        Map<String, Object> body = Map.of("name", "Dup", "type", "WALLET", "currency", "USD", "balance", 0);
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data.code").value("CONFLICT"));
    }

    @Test
    void updateChangesFields() throws Exception {
        String token = register(email(), password).get("accessToken");
        String id = createAccount(token, "Old", "CHECKING", "USD", 100);

        mockMvc.perform(patch("/api/accounts/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Renamed", "currency", "EUR", "institution", "Nova Bank"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Renamed"))
                .andExpect(jsonPath("$.data.currency").value("EUR"))
                .andExpect(jsonPath("$.data.institution").value("Nova Bank"))
                .andExpect(jsonPath("$.data.balance").value(100));
    }

    @Test
    void deleteDeactivatesRatherThanHardDeletes() throws Exception {
        String token = register(email(), password).get("accessToken");
        String id = createAccount(token, "ToGo", "CASH", "USD", 50);

        mockMvc.perform(delete("/api/accounts/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        // Still fetchable (history preserved) but flagged inactive.
        mockMvc.perform(get("/api/accounts/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        // Reactivate through the same update path.
        mockMvc.perform(patch("/api/accounts/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("active", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void userCannotAccessAnotherUsersAccount() throws Exception {
        String ownerToken = register(email(), password).get("accessToken");
        String ownerAccountId = createAccount(ownerToken, "Private", "CHECKING", "USD", 0);

        String otherToken = register(email(), password).get("accessToken");
        mockMvc.perform(get("/api/accounts/" + ownerAccountId).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.code").value("RESOURCE_NOT_FOUND"));
    }
}
