package com.nova.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.common.api.ApiResponse;
import com.nova.common.exception.ApiError;
import com.nova.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Produces the standard {@link ApiResponse} envelope for unauthenticated requests
 * to protected routes, replacing Spring Security's basic {@code 401}.
 */
@Component
public class NovaAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public NovaAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(ErrorCode.UNAUTHORIZED.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError body = ApiError.of(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getDefaultMessage());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error("Authentication required.", body));
    }
}
