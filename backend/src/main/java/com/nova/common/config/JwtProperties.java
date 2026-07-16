package com.nova.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration sourced from environment variables so deployments can rotate
 * secrets and tune lifetimes without rebuilding. Binds to the {@code nova.jwt.*}
 * namespace.
 *
 * <p>The signing secret MUST be provided (via {@code JWT_SECRET}) in shared and
 * production environments. A local-only development default is used when the
 * variable is absent.</p>
 */
@ConfigurationProperties(prefix = "nova.jwt")
public record JwtProperties(
        String secret,
        Long accessTokenMinutes,
        Long refreshTokenDays
) {
}
