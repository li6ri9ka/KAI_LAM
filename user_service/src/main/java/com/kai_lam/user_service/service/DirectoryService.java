package com.kai_lam.user_service.service;

import com.kai_lam.user_service.dto.*;
import com.kai_lam.user_service.exception.ConflictException;
import com.kai_lam.user_service.exception.NotFoundException;
import com.kai_lam.user_service.kafka.DomainEventPublisher;
import com.kai_lam.user_service.kafka.KafkaTopicsProperties;
import com.kai_lam.user_service.model.NameSpecialty;
import com.kai_lam.user_service.model.Team;
import com.kai_lam.user_service.model.UserProfile;
import com.kai_lam.user_service.model.UserTeam;
import com.kai_lam.user_service.repository.NameSpecialtyRepository;
import com.kai_lam.user_service.repository.TeamRepository;
import com.kai_lam.user_service.repository.UserTeamRepository;
import com.kai_lam.user_service.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DirectoryService {
    private final NameSpecialtyRepository nameSpecialtyRepository;
    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    private final UserService userService;
    private final DomainEventPublisher eventPublisher;
    private final KafkaTopicsProperties kafkaTopics;

    public DirectoryService(NameSpecialtyRepository nameSpecialtyRepository,
                            TeamRepository teamRepository,
                            UserTeamRepository userTeamRepository,
                            UserService userService,
                            DomainEventPublisher eventPublisher,
                            KafkaTopicsProperties kafkaTopics) {
        this.nameSpecialtyRepository = nameSpecialtyRepository;
        this.teamRepository = teamRepository;
        this.userTeamRepository = userTeamRepository;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.kafkaTopics = kafkaTopics;
    }

    @Transactional(readOnly = true)
    public List<NameSpecialtyResponse> listSpecialties(AuthPrincipal principal) {
        userService.ensureLocalUser(principal);
        return nameSpecialtyRepository.findAll().stream()
                .map(s -> new NameSpecialtyResponse(s.getIdNameSpecialty(), s.getNameSpecialty()))
                .toList();
    }

    @Transactional
    public NameSpecialtyResponse createSpecialty(AuthPrincipal principal, CreateNameSpecialtyRequest request) {
        userService.ensureLocalUser(principal);
        userService.ensureAdmin(principal);

        String name = request.nameSpecialty().trim();
        if (nameSpecialtyRepository.findByNameSpecialtyIgnoreCase(name).isPresent()) {
            throw new ConflictException("Specialty already exists");
        }

        NameSpecialty specialty = new NameSpecialty();
        specialty.setNameSpecialty(name);
        NameSpecialty saved = nameSpecialtyRepository.save(specialty);
        eventPublisher.publish(
                kafkaTopics.directoryEvents(),
                saved.getIdNameSpecialty().toString(),
                "SPECIALTY_CREATED",
                principal.getAuthUserId(),
                Map.of(
                        "idNameSpecialty", saved.getIdNameSpecialty().toString(),
                        "nameSpecialty", saved.getNameSpecialty()
                )
        );
        return new NameSpecialtyResponse(saved.getIdNameSpecialty(), saved.getNameSpecialty());
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> listTeams(AuthPrincipal principal) {
        userService.ensureLocalUser(principal);
        if (userService.isAdmin(principal)) {
            return teamRepository.findAll().stream().map(t -> new TeamResponse(t.getIdTeam(), t.getNameTeam())).toList();
        }

        return userTeamRepository.findAllByUserIdUser(principal.getAuthUserId())
                .stream()
                .map(UserTeam::getTeam)
                .map(t -> new TeamResponse(t.getIdTeam(), t.getNameTeam()))
                .distinct()
                .toList();
    }

    @Transactional
    public TeamResponse createTeam(AuthPrincipal principal, CreateTeamRequest request) {
        userService.ensureLocalUser(principal);
        userService.ensureAdmin(principal);

        String name = request.nameTeam().trim();
        if (teamRepository.findByNameTeamIgnoreCase(name).isPresent()) {
            throw new ConflictException("Team already exists");
        }

        Team team = new Team();
        team.setNameTeam(name);
        Team saved = teamRepository.save(team);
        eventPublisher.publish(
                kafkaTopics.directoryEvents(),
                saved.getIdTeam().toString(),
                "TEAM_CREATED",
                principal.getAuthUserId(),
                Map.of(
                        "idTeam", saved.getIdTeam().toString(),
                        "nameTeam", saved.getNameTeam()
                )
        );
        return new TeamResponse(saved.getIdTeam(), saved.getNameTeam());
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> listUserTeams(AuthPrincipal principal, UUID userId) {
        userService.ensureLocalUser(principal);
        UserProfile user = userService.findById(userId);

        if (!userService.isAdmin(principal)
                && !user.getIdUser().equals(principal.getAuthUserId())
                && !userTeamRepository.existsSharedTeam(principal.getAuthUserId(), userId)) {
            throw new com.kai_lam.user_service.exception.ForbiddenException("You can access only shared teams");
        }

        return userTeamRepository.findAllByUserIdUser(userId).stream()
                .map(UserTeam::getTeam)
                .map(t -> new TeamResponse(t.getIdTeam(), t.getNameTeam()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserTeamMembershipResponse> listUserTeamMemberships(AuthPrincipal principal, UUID userId) {
        userService.ensureLocalUser(principal);
        UserProfile user = userService.findById(userId);

        if (!userService.isAdmin(principal)
                && !user.getIdUser().equals(principal.getAuthUserId())
                && !userTeamRepository.existsSharedTeam(principal.getAuthUserId(), userId)) {
            throw new com.kai_lam.user_service.exception.ForbiddenException("You can access only shared teams");
        }

        return userTeamRepository.findAllByUserIdUser(userId).stream()
                .map(ut -> new UserTeamMembershipResponse(
                        ut.getIdUserTeam(),
                        ut.getUser().getIdUser(),
                        ut.getTeam().getIdTeam(),
                        ut.getTeam().getNameTeam()
                ))
                .toList();
    }

    @Transactional
    public void addUserToTeam(AuthPrincipal principal, UUID userId, UUID teamId) {
        userService.ensureLocalUser(principal);
        userService.ensureAdmin(principal);

        if (userTeamRepository.existsByTeamIdTeamAndUserIdUser(teamId, userId)) {
            throw new ConflictException("User is already in this team");
        }

        UserProfile user = userService.findById(userId);
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found"));

        UserTeam userTeam = new UserTeam();
        userTeam.setUser(user);
        userTeam.setTeam(team);
        userTeamRepository.save(userTeam);
        eventPublisher.publish(
                kafkaTopics.directoryEvents(),
                userId.toString(),
                "USER_ADDED_TO_TEAM",
                principal.getAuthUserId(),
                Map.of(
                        "idUser", userId.toString(),
                        "idTeam", teamId.toString()
                )
        );
    }

    @Transactional
    public void removeUserFromTeam(AuthPrincipal principal, UUID userId, UUID teamId) {
        userService.ensureLocalUser(principal);
        userService.ensureAdmin(principal);

        UserTeam userTeam = userTeamRepository.findByTeamIdTeamAndUserIdUser(teamId, userId)
                .orElseThrow(() -> new NotFoundException("User team membership not found"));
        userTeamRepository.delete(userTeam);
        eventPublisher.publish(
                kafkaTopics.directoryEvents(),
                userId.toString(),
                "USER_REMOVED_FROM_TEAM",
                principal.getAuthUserId(),
                Map.of(
                        "idUser", userId.toString(),
                        "idTeam", teamId.toString()
                )
        );
    }
}
