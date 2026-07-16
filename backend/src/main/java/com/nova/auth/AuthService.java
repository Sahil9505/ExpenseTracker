package com.nova.auth;

import com.nova.auth.security.JwtService;
import com.nova.auth.security.RefreshTokenService;
import com.nova.auth.web.dto.AuthResponse;
import com.nova.auth.web.dto.LoginRequest;
import com.nova.auth.web.dto.RefreshRequest;
import com.nova.auth.web.dto.RegisterRequest;
import com.nova.auth.web.dto.LogoutRequest;
import com.nova.common.config.JwtProperties;
import com.nova.common.exception.ConflictException;
import com.nova.common.exception.InvalidCredentialsException;
import com.nova.common.exception.InvalidTokenException;
import com.nova.user.AccountStatus;
import com.nova.user.Role;
import com.nova.user.User;
import com.nova.user.UserRepository;
import com.nova.user.mapper.UserMapper;
import com.nova.user.web.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Authentication flows: registration, login, refresh-token rotation, and logout.
 * Holds the orchestration logic so controllers stay thin.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final long accessTokenMinutes;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenService refreshTokenService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserMapper userMapper,
            JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.accessTokenMinutes = jwtProperties.accessTokenMinutes();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists.");
        }
        String currency = request.preferredCurrency() != null
                ? request.preferredCurrency().toUpperCase() : "USD";
        User user = new User(
                request.email(),
                request.fullName(),
                passwordEncoder.encode(request.password()),
                Role.USER,
                AccountStatus.ACTIVE,
                currency);
        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        user.setLastLoginAt(Instant.now());
        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String rawToken) {
        UUID userId = refreshTokenService.validate(rawToken);
        User user = userRepository.findById(userId).orElseThrow(InvalidTokenException::new);
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTokenException();
        }
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.rotate(rawToken);
        return AuthResponse.of(newAccessToken, newRefreshToken, accessExpirySeconds(), userMapper.toResponse(user));
    }

    @Transactional
    public void logout(UUID userId, String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            refreshTokenService.revoke(rawToken);
        } else {
            refreshTokenService.revokeAllForUser(userId);
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user);
        return AuthResponse.of(accessToken, refreshToken, accessExpirySeconds(), userMapper.toResponse(user));
    }

    private long accessExpirySeconds() {
        return accessTokenMinutes * 60L;
    }
}
