package com.nova.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.common.api.ApiResponse;
import com.nova.common.exception.ApiError;
import com.nova.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Produces the standard {@link ApiResponse} envelope for authenticated requests
 * that are not permitted (role/authority failure), replacing the basic 403 page.
 */
@Component
public class NovaAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public NovaAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(ErrorCode.FORBIDDEN.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError body = ApiError.of(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getDefaultMessage());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error("Access denied.", body));
    }
}
