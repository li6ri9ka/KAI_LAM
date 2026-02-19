package com.kai_lam.auth_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "role_user")
public class RoleUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_role", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name_role", nullable = false, unique = true, length = 30)
    private String nameRole;

    @OneToMany(mappedBy = "role")
    private Set<AuthUser> users;
}
