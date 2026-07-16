package com.nova.auth;

import com.nova.auth.security.NovaUserPrincipal;
import com.nova.auth.web.dto.AuthResponse;
import com.nova.auth.web.dto.LoginRequest;
import com.nova.auth.web.dto.LogoutRequest;
import com.nova.auth.web.dto.RefreshRequest;
import com.nova.auth.web.dto.RegisterRequest;
import com.nova.common.api.ApiResponse;
import com.nova.user.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Registration successful.", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful.", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok("Token refreshed.", authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal NovaUserPrincipal principal,
            @Valid @RequestBody(required = false) LogoutRequest request) {
        authService.logout(principal.getUserId(), request != null ? request.refreshToken() : null);
        return ApiResponse.ok("Logged out.");
    }
}
