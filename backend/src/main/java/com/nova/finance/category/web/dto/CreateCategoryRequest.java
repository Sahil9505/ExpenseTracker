package com.nova.finance.category.web.dto;

import com.nova.finance.category.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload for creating a user category. The type (income or expense) is fixed at
 * creation and cannot be changed later because transactions depend on it.
 */
public record CreateCategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 120, message = "Category name is too long")
        String name,

        @NotNull(message = "Category type is required")
        Category.Type type,

        @Size(max = 32, message = "Color is too long")
        String color,

        @Size(max = 64, message = "Icon is too long")
        String icon
) {
}
