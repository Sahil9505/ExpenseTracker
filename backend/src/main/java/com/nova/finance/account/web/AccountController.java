package com.nova.finance.account.web;

import com.nova.auth.security.NovaUserPrincipal;
import com.nova.common.api.ApiResponse;
import com.nova.finance.account.AccountService;
import com.nova.finance.account.web.dto.AccountResponse;
import com.nova.finance.account.web.dto.CreateAccountRequest;
import com.nova.finance.account.web.dto.UpdateAccountRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ApiResponse<List<AccountResponse>> listAccounts(
            @AuthenticationPrincipal NovaUserPrincipal principal) {
        return ApiResponse.ok(accountService.listAccounts(principal.getUserId()));
    }

    @PostMapping
    public ApiResponse<AccountResponse> createAccount(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @Valid @RequestBody CreateAccountRequest request) {
        return ApiResponse.ok("Account created.", accountService.createAccount(principal.getUserId(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountResponse> getAccount(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @PathVariable UUID id) {
        return ApiResponse.ok(accountService.getAccount(principal.getUserId(), id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<AccountResponse> updateAccount(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request) {
        return ApiResponse.ok("Account updated.", accountService.updateAccount(principal.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAccount(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @PathVariable UUID id) {
        accountService.deleteAccount(principal.getUserId(), id);
        return ApiResponse.ok("Account deactivated.");
    }
}
