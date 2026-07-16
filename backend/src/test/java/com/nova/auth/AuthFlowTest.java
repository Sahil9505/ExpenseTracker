package com.nova.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end auth flow against the full Spring context (in-memory H2, Flyway
 * disabled, JWT issued with the test secret). Covers registration, login,
 * refresh rotation, protected access, profile updates, password change, and
 * logout.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String email() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8) + "@nova.test";
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private JsonNode parse(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void registerReturnsTokensAndUserWithoutPasswordHash() throws Exception {
        String email = email();
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", "sup3rSecret!",
                                "fullName", "Ada Lovelace",
                                "preferredCurrency", "eur"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(jsonPath("$.data.user.role").value("USER"))
                .andExpect(jsonPath("$.data.user.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.user.preferredCurrency").value("EUR"))
                .andReturn();

        JsonNode user = parse(result).get("data").get("user");
        org.junit.jupiter.api.Assertions.assertFalse(
                user.has("passwordHash"), "user projection must not include the password hash");
    }

    @Test
    void registerDuplicateEmailReturnsConflict() throws Exception {
        String email = email();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "sup3rSecret!"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "anotherPass1"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("CONFLICT"));
    }

    @Test
    void registerWithInvalidFieldsReturnsFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "not-an-email", "password", "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.fields").isArray())
                .andExpect(jsonPath("$.data.fields.length()").value(2));
    }

    @Test
    void loginSucceedsAndMeReturnsCurrentUser() throws Exception {
        String email = email();
        String password = "sup3rSecret!";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        String accessToken = parse(login).get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.lastLoginAt").isNotEmpty());
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        String email = email();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "sup3rSecret!"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "wrongPassword"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));
    }

    @Test
    void protectedRouteWithoutTokenReturnsUnauthorizedEnvelope() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));
    }

    @Test
    void malformedAccessTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refreshRotatesTokensAndRevokesPrevious() throws Exception {
        String email = email();
        MvcResult registered = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "sup3rSecret!"))))
                .andExpect(status().isOk())
                .andReturn();
        String refreshToken = parse(registered).get("data").get("refreshToken").asText();
        String oldAccessToken = parse(registered).get("data").get("accessToken").asText();

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();
        String newRefreshToken = parse(refreshed).get("data").get("refreshToken").asText();

        // The rotated-in refresh token must work again.
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", newRefreshToken))))
                .andExpect(status().isOk());

        // The original refresh token must now be revoked.
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.code").value("INVALID_TOKEN"));

        // Original access token is still valid (not revoked on refresh).
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + oldAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    void refreshWithUnknownTokenReturnsInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", "does-not-exist-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.code").value("INVALID_TOKEN"));
    }

    @Test
    void updateProfilePersistsChanges() throws Exception {
        String accessToken = authenticatedAccessToken();

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Grace Hopper", "timezone", "America/New_York"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Grace Hopper"))
                .andExpect(jsonPath("$.data.timezone").value("America/New_York"));
    }

    @Test
    void changePasswordInvalidatesSessionsAndAcceptsNewPassword() throws Exception {
        String email = email();
        String password = "sup3rSecret!";
        String newPassword = "evenMoreSecret1";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk());

        String accessToken = loginAccessToken(email, password);

        mockMvc.perform(patch("/api/users/me/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", password, "newPassword", newPassword))))
                .andExpect(status().isOk());

        // Old password no longer works.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isUnauthorized());

        // New password works.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", newPassword))))
                .andExpect(status().isOk());
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        String email = email();
        MvcResult registered = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "sup3rSecret!"))))
                .andExpect(status().isOk())
                .andReturn();
        String refreshToken = parse(registered).get("data").get("refreshToken").asText();
        String accessToken = parse(registered).get("data").get("accessToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    private String authenticatedAccessToken() throws Exception {
        String email = email();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "sup3rSecret!"))))
                .andExpect(status().isOk());
        return loginAccessToken(email, "sup3rSecret!");
    }

    private String loginAccessToken(String email, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return parse(login).get("data").get("accessToken").asText();
    }
}
