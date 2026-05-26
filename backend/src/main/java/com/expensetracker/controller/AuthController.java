package com.expensetracker.controller;

import com.expensetracker.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.expensetracker.dto.ApiResponse;

@RestController
@RequestMapping("/api/auth") // Updated to /api/auth as requested originally
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");

            // Validation
            if (name == null || name.trim().isEmpty() || 
                email == null || email.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Name, email, and password are required."));
            }
            if (!email.contains("@")) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Please provide a valid email address."));
            }
            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Password must be at least 6 characters long."));
            }

            Map<String, String> data = authService.register(name, email, password);
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", data));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email and password are required."));
            }

            Map<String, String> data = authService.login(email, password);
            return ResponseEntity.ok(ApiResponse.success("Login successful", data));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid email or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        try {
            return ResponseEntity.ok(ApiResponse.success("User fetched successfully", authService.getCurrentUser()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
    }
}
