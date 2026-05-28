package com.ycyw.chat.chat_poc_backend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserPrincipal implements UserDetails {

    private final String username;
    private final String role;
    private final UUID customerId;
    private final String agentId;

    public UserPrincipal(String username, String role, UUID customerId, String agentId) {
        this.username = username;
        this.role = role;
        this.customerId = customerId;
        this.agentId = agentId;
    }

    public String getRole() { return role; }
    public UUID getCustomerId() { return customerId; }
    public String getAgentId() { return agentId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() { return ""; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
