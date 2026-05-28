package com.ycyw.chat.chat_poc_backend.security;

import com.ycyw.chat.chat_poc_backend.config.AppSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final AppSecurityProperties properties;
    private final SecretKey key;

    public JwtService(AppSecurityProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createClientToken(UUID customerId, String email) {
        return buildToken(email, "CLIENT", customerId, null);
    }

    public String createAdvisorToken(String agentId, String agentName) {
        return buildToken(agentName, "ADVISOR", null, agentId);
    }

    private String buildToken(String subject, String role, UUID customerId, String agentId) {
        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + properties.getJwtExpirationMs()));

        if (customerId != null) {
            builder.claim("customerId", customerId.toString());
        }
        if (agentId != null) {
            builder.claim("agentId", agentId);
        }
        return builder.signWith(key).compact();
    }

    public UserPrincipal parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String role = claims.get("role", String.class);
        UUID customerId = claims.get("customerId") != null
                ? UUID.fromString(claims.get("customerId", String.class))
                : null;
        String agentId = claims.get("agentId", String.class);
        return new UserPrincipal(claims.getSubject(), role, customerId, agentId);
    }
}
