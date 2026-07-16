package com.nova.finance.transaction.web;

import com.nova.auth.security.NovaUserPrincipal;
import com.nova.common.api.ApiResponse;
import com.nova.finance.transaction.TransactionFilter;
import com.nova.finance.transaction.TransactionService;
import com.nova.finance.transaction.web.dto.CreateTransactionRequest;
import com.nova.finance.transaction.web.dto.TransactionResponse;
import com.nova.finance.transaction.web.dto.UpdateTransactionRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ApiResponse<List<TransactionResponse>> listTransactions(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @RequestParam(required = false) com.nova.finance.transaction.Transaction.Type type,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) String search) {
        TransactionFilter filter = new TransactionFilter(type, accountId, categoryId, from, to, search);
        return ApiResponse.ok(transactionService.list(principal.getUserId(), filter));
    }

    @PostMapping
    public ApiResponse<TransactionResponse> createTransaction(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @Valid @RequestBody CreateTransactionRequest request) {
        return ApiResponse.ok("Transaction recorded.", transactionService.create(principal.getUserId(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> getTransaction(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @PathVariable UUID id) {
        return ApiResponse.ok(transactionService.getTransaction(principal.getUserId(), id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        return ApiResponse.ok("Transaction updated.", transactionService.update(principal.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTransaction(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @PathVariable UUID id) {
        transactionService.delete(principal.getUserId(), id);
        return ApiResponse.ok("Transaction deleted.");
    }
}
