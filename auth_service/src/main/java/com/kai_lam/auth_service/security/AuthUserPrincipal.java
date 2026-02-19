package com.kai_lam.auth_service.security;

import com.kai_lam.auth_service.kai_enum.AccountEnum;
import com.kai_lam.auth_service.model.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AuthUserPrincipal implements UserDetails {
    private final UUID id;
    private final String login;
    private final String passwordHash;
    private final String role;
    private final AccountEnum status;

    public AuthUserPrincipal(AuthUser user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole().getNameRole();
        this.status = user.getStatus();
    }

    public UUID getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != AccountEnum.BLOCKED;
    }

    @Override
    public boolean isEnabled() {
        return status == AccountEnum.ACTIVE;
    }
}
