package com.expensetracker.controller;

import com.expensetracker.dto.ApiResponse;
import com.expensetracker.service.ExpenseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private ExpenseCategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success("Categories fetched successfully", categoryService.getAllCategories()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addCategory(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String icon = request.get("icon");
            String color = request.get("color");

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Name is required"));
            }
            return ResponseEntity.ok(ApiResponse.success("Category added", categoryService.addCategory(name, icon, color)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateCategory(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String icon = request.get("icon");
            String color = request.get("color");

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Name is required"));
            }
            return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.updateCategory(id, name, icon, color)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
