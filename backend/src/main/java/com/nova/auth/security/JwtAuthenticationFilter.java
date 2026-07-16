package com.nova.auth.security;

import com.nova.common.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Stateless JWT filter. If a valid Bearer token is present and belongs to an
 * active, unlocked account, the request proceeds with an authenticated principal.
 * If the token is missing, invalid, or its account is disabled, locked, or still
 * pending, the request continues unauthenticated so protected routes are rejected
 * with a standard {@code 401} envelope by the authentication entry point. Token
 * values are never logged.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                String email = jwtService.extractSubject(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(token, userDetails.getUsername())
                        && userDetails.isEnabled()
                        && userDetails.isAccountNonLocked()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Invalid token: leave unauthenticated. Do not log the token value.
                if (log.isDebugEnabled()) {
                    log.debug("Rejected JWT for {} {}", request.getMethod(), request.getRequestURI());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
