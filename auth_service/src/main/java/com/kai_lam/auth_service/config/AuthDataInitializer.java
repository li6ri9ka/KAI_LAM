package com.kai_lam.auth_service.config;

import com.kai_lam.auth_service.model.RoleUser;
import com.kai_lam.auth_service.repository.RoleUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthDataInitializer implements ApplicationRunner {
    private final RoleUserRepository roleUserRepository;

    public AuthDataInitializer(RoleUserRepository roleUserRepository) {
        this.roleUserRepository = roleUserRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureRole("USER");
        ensureRole("ADMIN");
    }

    private void ensureRole(String name) {
        if (roleUserRepository.findByNameRoleIgnoreCase(name).isPresent()) {
            return;
        }

        RoleUser role = new RoleUser();
        role.setNameRole(name);
        roleUserRepository.save(role);
    }
}
