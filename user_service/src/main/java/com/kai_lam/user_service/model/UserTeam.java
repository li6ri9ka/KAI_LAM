package com.kai_lam.user_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user_team", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_team", columnNames = {"user_id", "team_id"})
})
public class UserTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_user_team", nullable = false, updatable = false)
    private UUID idUserTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    public UUID getIdUserTeam() {
        return idUserTeam;
    }

    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
