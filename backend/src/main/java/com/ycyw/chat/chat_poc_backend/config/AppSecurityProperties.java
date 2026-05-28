package com.ycyw.chat.chat_poc_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private List<String> corsOrigins = List.of("http://localhost:4200");
    private String jwtSecret = "change-me";
    private long jwtExpirationMs = 86400000L;
    private int rateLimitPerMinute = 120;

    public List<String> getCorsOrigins() { return corsOrigins; }
    public void setCorsOrigins(List<String> corsOrigins) { this.corsOrigins = corsOrigins; }

    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }

    public long getJwtExpirationMs() { return jwtExpirationMs; }
    public void setJwtExpirationMs(long jwtExpirationMs) { this.jwtExpirationMs = jwtExpirationMs; }

    public int getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
}
