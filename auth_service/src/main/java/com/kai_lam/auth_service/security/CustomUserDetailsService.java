package com.kai_lam.auth_service.security;

import com.kai_lam.auth_service.model.AuthUser;
import com.kai_lam.auth_service.repository.AuthUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AuthUserRepository authUserRepository;

    public CustomUserDetailsService(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser user = authUserRepository.findByLoginIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new AuthUserPrincipal(user);
    }

    public UserDetails loadUserById(UUID id) {
        AuthUser user = authUserRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new AuthUserPrincipal(user);
    }
}
