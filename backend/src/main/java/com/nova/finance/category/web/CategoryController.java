package com.nova.finance.category.web;

import com.nova.auth.security.NovaUserPrincipal;
import com.nova.common.api.ApiResponse;
import com.nova.finance.category.CategoryService;
import com.nova.finance.category.web.dto.CategoryResponse;
import com.nova.finance.category.web.dto.CreateCategoryRequest;
import com.nova.finance.category.web.dto.UpdateCategoryRequest;
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
@RequestMapping("/api/categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> listCategories(
            @AuthenticationPrincipal NovaUserPrincipal principal) {
        return ApiResponse.ok(categoryService.listCategories(principal.getUserId()));
    }

    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @Valid @RequestBody CreateCategoryRequest request) {
        return ApiResponse.ok("Category created.", categoryService.createCategory(principal.getUserId(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategory(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @PathVariable UUID id) {
        return ApiResponse.ok(categoryService.getCategory(principal.getUserId(), id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ApiResponse.ok("Category updated.", categoryService.updateCategory(principal.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @PathVariable UUID id) {
        categoryService.deleteCategory(principal.getUserId(), id);
        return ApiResponse.ok("Category deleted.");
    }
}
