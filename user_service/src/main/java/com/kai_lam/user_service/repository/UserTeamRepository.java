package com.kai_lam.user_service.repository;

import com.kai_lam.user_service.model.UserTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTeamRepository extends JpaRepository<UserTeam, UUID> {
    boolean existsByTeamIdTeamAndUserIdUser(UUID teamId, UUID userId);

    List<UserTeam> findAllByUserIdUser(UUID userId);

    List<UserTeam> findAllByTeamIdTeam(UUID teamId);

    Optional<UserTeam> findByTeamIdTeamAndUserIdUser(UUID teamId, UUID userId);

    @Query("""
            select case when count(ut) > 0 then true else false end
            from UserTeam ut
            where ut.user.idUser = :authUserId
              and ut.team.idTeam in (
                    select ut2.team.idTeam from UserTeam ut2 where ut2.user.idUser = :targetUserId
              )
            """)
    boolean existsSharedTeam(@Param("authUserId") UUID authUserId, @Param("targetUserId") UUID targetUserId);
}
