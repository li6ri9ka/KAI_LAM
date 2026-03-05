package com.kai_lam.auth_service.service;

import com.kai_lam.auth_service.dto.LoginRequest;
import com.kai_lam.auth_service.dto.RegisterRequest;
import com.kai_lam.auth_service.exception.ConflictException;
import com.kai_lam.auth_service.kai_enum.RoleEnum;
import com.kai_lam.auth_service.model.RoleUser;
import com.kai_lam.auth_service.repository.AuthUserRepository;
import com.kai_lam.auth_service.repository.RoleUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceFlowTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private RoleUserRepository roleUserRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    @BeforeEach
    void ensureRoles() {
        ensureRole(RoleEnum.USER.name());
        ensureRole(RoleEnum.ADMIN.name());
    }

    @Test
    void tc001_registersNewUserSuccessfully() {
        var result = authService.register(new RegisterRequest("new.user", "new.user@mail.test", "Password123"), 7);

        assertThat(result.response().accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.response().user().login()).isEqualTo("new.user");
        assertThat(result.response().user().role()).isEqualTo("USER");
        assertThat(authUserRepository.existsByEmailIgnoreCase("new.user@mail.test")).isTrue();
    }

    @Test
    void tc002_rejectsRegistrationWithDuplicateEmail() {
        authService.register(new RegisterRequest("first.user", "dup@mail.test", "Password123"), 7);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("second.user", "dup@mail.test", "Password123"),
                7
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");
    }

    @Test
    void tc003_loginsWithValidCredentials() {
        authService.register(new RegisterRequest("login.user", "login.user@mail.test", "Password123"), 7);

        var result = authService.login(new LoginRequest("login.user", "Password123"), 7);

        assertThat(result.response().accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.response().user().email()).isEqualTo("login.user@mail.test");
    }

    private void ensureRole(String roleName) {
        roleUserRepository.findByNameRoleIgnoreCase(roleName).orElseGet(() -> {
            RoleUser role = new RoleUser();
            role.setNameRole(roleName);
            return roleUserRepository.save(role);
        });
    }
}
