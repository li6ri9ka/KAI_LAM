package com.kai_lam.user_service.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_team", nullable = false, updatable = false)
    private UUID idTeam;

    @Column(name = "name_team", nullable = false, length = 120, unique = true)
    private String nameTeam;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserTeam> userTeams = new HashSet<>();

    public UUID getIdTeam() {
        return idTeam;
    }

    public String getNameTeam() {
        return nameTeam;
    }

    public void setNameTeam(String nameTeam) {
        this.nameTeam = nameTeam;
    }

    public Set<UserTeam> getUserTeams() {
        return userTeams;
    }
}
