package com.nova.auth.security;

import com.nova.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Issues and verifies the short-lived JWT access token. The subject of the token
 * is the user's email; the user id and role are carried as claims so the
 * authentication filter can rebuild a principal without an extra query beyond the
 * user lookup.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenMinutes;

    public JwtService(com.nova.common.config.JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = props.accessTokenMinutes();
    }

    /** Creates a signed access token for the given user. */
    public String generateAccessToken(User user) {
        long now = System.currentTimeMillis();
        long expiry = now + accessTokenMinutes * 60_000L;
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId().toString())
                .claim("role", "ROLE_" + user.getRole().name())
                .issuedAt(new Date(now))
                .expiration(new Date(expiry))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** Returns the token subject (the user email). */
    public String extractSubject(String token) {
        return parse(token).getSubject();
    }

    /** True when the token signature is valid, not expired, and matches the user. */
    public boolean isTokenValid(String token, String expectedEmail) {
        try {
            return extractSubject(token).equals(expectedEmail) && !isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return parse(token).getExpiration().before(new Date());
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
