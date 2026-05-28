package com.ycyw.chat.chat_poc_backend.security;

import com.ycyw.chat.chat_poc_backend.config.AppSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    
    @BeforeEach
    void setUp() {
        AppSecurityProperties props = new AppSecurityProperties();
        props.setJwtSecret("YCYW_Chat_POC_SecretKey_VeryLongString_For_HS256_Encryption_Min32");
        props.setJwtExpirationMs(86400000L);
        props.setRateLimitPerMinute(120);
        jwtService = new JwtService(props);
    }

    @Test
    void testJwtRoundTripClient() {
        UUID customerId = UUID.randomUUID();
        String token = jwtService.createClientToken(customerId, "client@test.com");
        
        UserPrincipal principal = jwtService.parseToken(token);
        
        assertNotNull(principal);
        assertEquals("CLIENT", principal.getRole());
        assertEquals(customerId, principal.getCustomerId());
        assertNull(principal.getAgentId());
    }

    @Test
    void testJwtRoundTripAdvisor() {
        String token = jwtService.createAdvisorToken("agent-001", "Sophie Martin");
        
        UserPrincipal principal = jwtService.parseToken(token);
        
        assertNotNull(principal);
        assertEquals("ADVISOR", principal.getRole());
        assertEquals("agent-001", principal.getAgentId());
        assertNull(principal.getCustomerId());
    }

    @Test
    void testInvalidTokenThrows() {
        assertThrows(Exception.class, () -> jwtService.parseToken("invalid.token.here"));
    }

    @Test
    void testUserPrincipalAuthorities() {
        String token = jwtService.createAdvisorToken("agent-001", "Sophie");
        UserPrincipal principal = jwtService.parseToken(token);
        
        assertTrue(principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADVISOR")));
    }
}
