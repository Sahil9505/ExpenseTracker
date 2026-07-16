package com.nova.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.user.AccountStatus;
import com.nova.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves that a non-ACTIVE account (disabled, locked, or still pending) cannot use
 * a previously issued JWT to reach protected resources, nor rotate a refresh token.
 * Registration always yields an ACTIVE account, so each case flips the stored
 * status after signing up and then exercises the endpoints with the original tokens.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccountStatusEnforcementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String email() {
        return "status-" + UUID.randomUUID().toString().substring(0, 8) + "@nova.test";
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private JsonNode parse(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Map<String, String> registerAndCapture(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = parse(result).get("data");
        return Map.of(
                "accessToken", data.get("accessToken").asText(),
                "refreshToken", data.get("refreshToken").asText());
    }

    private void setStatus(String email, AccountStatus status) {
        userRepository.findByEmail(email).ifPresent(u -> {
            u.setAccountStatus(status);
            userRepository.save(u);
        });
    }

    @Test
    void disabledUserCannotAccessMeOrRefresh() throws Exception {
        assertNonActiveUserRejected(AccountStatus.DISABLED);
    }

    @Test
    void lockedUserCannotAccessMeOrRefresh() throws Exception {
        assertNonActiveUserRejected(AccountStatus.LOCKED);
    }

    @Test
    void pendingUserCannotAccessMeOrRefresh() throws Exception {
        assertNonActiveUserRejected(AccountStatus.PENDING);
    }

    private void assertNonActiveUserRejected(AccountStatus status) throws Exception {
        String email = email();
        String password = "sup3rSecret!";
        Map<String, String> tokens = registerAndCapture(email, password);
        setStatus(email, status);

        // A validly-signed JWT for a non-active account must not grant access.
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + tokens.get("accessToken")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));

        // The refresh flow must also reject non-active accounts.
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", tokens.get("refreshToken")))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("INVALID_TOKEN"));
    }
}
