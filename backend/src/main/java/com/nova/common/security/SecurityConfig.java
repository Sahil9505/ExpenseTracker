package com.nova.common.security;

import com.nova.auth.security.JwtAuthenticationFilter;
import com.nova.auth.security.NovaAccessDeniedHandler;
import com.nova.auth.security.NovaAuthenticationEntryPoint;
import com.nova.auth.security.UserDetailsServiceImpl;
import com.nova.common.config.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Stateless JWT security configuration for Phase 2.
 *
 * <p>Public routes are limited to health, documentation, and the unauthenticated
 * auth endpoints (register, login, refresh). Everything else requires a valid
 * access token. The authentication entry point and access-denied handler return
 * the standard {@link com.nova.common.api.ApiResponse} envelope.</p>
 */
@Configuration
@EnableWebSecurity
@org.springframework.boot.context.properties.EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String[] PUBLIC_MATCHERS = {
            "/api/health",
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/error"
    };

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            NovaAuthenticationEntryPoint authenticationEntryPoint,
            NovaAccessDeniedHandler accessDeniedHandler,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new org.springframework.security.authentication.ProviderManager(provider);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            com.nova.auth.security.JwtService jwtService,
            UserDetailsServiceImpl userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }
}
