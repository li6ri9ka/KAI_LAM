package com.kai_lam.user_service.repository;

import com.kai_lam.user_service.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByNameTeamIgnoreCase(String nameTeam);
}
