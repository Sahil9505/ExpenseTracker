package com.nova.finance.category.web.dto;

import jakarta.validation.constraints.Size;

/**
 * Partial update for a category. System categories may have their color/icon
 * changed but not their name or type. User categories may change anything except
 * their type.
 */
public record UpdateCategoryRequest(

        @Size(max = 120, message = "Category name is too long")
        String name,

        @Size(max = 32, message = "Color is too long")
        String color,

        @Size(max = 64, message = "Icon is too long")
        String icon
) {
}
