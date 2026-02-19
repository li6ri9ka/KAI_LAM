package com.kai_lam.user_service.security;

import com.kai_lam.user_service.model.GlobalRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AuthPrincipal implements UserDetails {
    private final UUID authUserId;
    private final String login;
    private final GlobalRole globalRole;

    public AuthPrincipal(UUID authUserId, String login, GlobalRole globalRole) {
        this.authUserId = authUserId;
        this.login = login;
        this.globalRole = globalRole;
    }

    public UUID getAuthUserId() {
        return authUserId;
    }

    public GlobalRole getGlobalRole() {
        return globalRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + globalRole.name()));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
