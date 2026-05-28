package com.ycyw.chat.chat_poc_backend;

import com.ycyw.chat.chat_poc_backend.config.AppSecurityProperties;
import com.ycyw.chat.chat_poc_backend.security.JwtService;
import com.ycyw.chat.chat_poc_backend.security.UserPrincipal;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void createAndParseClientToken() {
        AppSecurityProperties props = new AppSecurityProperties();
        props.setJwtSecret("012345678901234567890123456789012345678901234567890123456789");
        props.setJwtExpirationMs(60_000);

        JwtService jwtService = new JwtService(props);

        UUID customerId = UUID.randomUUID();
        String token = jwtService.createClientToken(customerId, "client@example.com");

        UserPrincipal principal = jwtService.parseToken(token);
        assertNotNull(principal);
        assertEquals("CLIENT", principal.getRole());
        assertEquals(customerId, principal.getCustomerId());
    }
}

