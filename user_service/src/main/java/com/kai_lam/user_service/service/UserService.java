package com.kai_lam.user_service.service;

import com.kai_lam.user_service.dto.MeUpdateRequest;
import com.kai_lam.user_service.dto.TeamResponse;
import com.kai_lam.user_service.dto.UserEligibilityResponse;
import com.kai_lam.user_service.dto.UserResponse;
import com.kai_lam.user_service.exception.BadRequestException;
import com.kai_lam.user_service.exception.ForbiddenException;
import com.kai_lam.user_service.exception.NotFoundException;
import com.kai_lam.user_service.kafka.DomainEventPublisher;
import com.kai_lam.user_service.kafka.KafkaTopicsProperties;
import com.kai_lam.user_service.model.GlobalRole;
import com.kai_lam.user_service.model.NameSpecialty;
import com.kai_lam.user_service.model.UserProfile;
import com.kai_lam.user_service.model.UserTeam;
import com.kai_lam.user_service.repository.NameSpecialtyRepository;
import com.kai_lam.user_service.repository.UserProfileRepository;
import com.kai_lam.user_service.repository.UserTeamRepository;
import com.kai_lam.user_service.security.AuthPrincipal;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserProfileRepository userProfileRepository;
    private final NameSpecialtyRepository nameSpecialtyRepository;
    private final UserTeamRepository userTeamRepository;
    private final DomainEventPublisher eventPublisher;
    private final KafkaTopicsProperties kafkaTopics;

    public UserService(UserProfileRepository userProfileRepository,
                       NameSpecialtyRepository nameSpecialtyRepository,
                       UserTeamRepository userTeamRepository,
                       DomainEventPublisher eventPublisher,
                       KafkaTopicsProperties kafkaTopics) {
        this.userProfileRepository = userProfileRepository;
        this.nameSpecialtyRepository = nameSpecialtyRepository;
        this.userTeamRepository = userTeamRepository;
        this.eventPublisher = eventPublisher;
        this.kafkaTopics = kafkaTopics;
    }

    @Transactional
    public UserResponse me(AuthPrincipal principal) {
        return toUserResponse(ensureLocalUser(principal));
    }

    @Transactional
    public UserResponse updateMe(AuthPrincipal principal, MeUpdateRequest request) {
        UserProfile user = ensureLocalUser(principal);
        if (request.nameUser() != null && !request.nameUser().isBlank()) {
            user.setNameUser(request.nameUser().trim());
        }
        if (request.midleNameUser() != null) {
            user.setMidleNameUser(request.midleNameUser().isBlank() ? null : request.midleNameUser().trim());
        }
        if (request.emailUser() != null) {
            user.setEmailUser(request.emailUser().isBlank() ? null : request.emailUser().trim().toLowerCase());
        }
        if (request.phoneUser() != null) {
            user.setPhoneUser(request.phoneUser().isBlank() ? null : request.phoneUser().trim());
        }
        if (request.telegramUser() != null) {
            user.setTelegramUser(request.telegramUser().isBlank() ? null : request.telegramUser().trim());
        }
        if (request.nameSpecUserId() != null) {
            NameSpecialty specialty = nameSpecialtyRepository.findById(request.nameSpecUserId())
                    .orElseThrow(() -> new NotFoundException("Specialty not found"));
            user.setNameSpecialty(specialty);
        } else if (request.nameSpecialty() != null && !request.nameSpecialty().isBlank()) {
            nameSpecialtyRepository.findByNameSpecialtyIgnoreCase(request.nameSpecialty().trim())
                    .ifPresent(user::setNameSpecialty);
        }
        UserProfile saved = userProfileRepository.save(user);
        publishUserEvent("USER_UPDATED", principal.getAuthUserId(), saved);
        return toUserResponse(saved);
    }

    @Transactional
    public UserResponse getUser(AuthPrincipal principal, UUID userId) {
        UserProfile current = ensureLocalUser(principal);
        UserProfile target = findById(userId);

        if (!isAdmin(principal)
                && !target.getIdUser().equals(current.getIdUser())
                && !userTeamRepository.existsSharedTeam(principal.getAuthUserId(), target.getIdUser())) {
            throw new ForbiddenException("You can view only your profile or users from shared teams");
        }

        return toUserResponse(target);
    }

    @Transactional
    public Page<UserResponse> listUsers(AuthPrincipal principal,
                                        String q,
                                        UUID teamId,
                                        UUID nameSpecUserId,
                                        Pageable pageable) {
        ensureLocalUser(principal);

        if (!isAdmin(principal)) {
            if (teamId == null) {
                throw new BadRequestException("teamId is required for non-admin users");
            }
            if (!userTeamRepository.existsByTeamIdTeamAndUserIdUser(teamId, principal.getAuthUserId())) {
                throw new ForbiddenException("You are not a member of this team");
            }
        }

        Specification<UserProfile> specification = (root, query, cb) -> cb.conjunction();
        if (q != null && !q.isBlank()) {
            specification = specification.and(searchByText(q));
        }
        if (teamId != null) {
            specification = specification.and(byTeam(teamId));
        }
        if (nameSpecUserId != null) {
            specification = specification.and(bySpecialty(nameSpecUserId));
        }

        return userProfileRepository.findAll(specification, pageable).map(this::toUserResponse);
    }

    @Transactional
    public void deleteUser(AuthPrincipal principal, UUID userId) {
        ensureAdmin(principal);
        UserProfile user = findById(userId);
        userProfileRepository.delete(user);
        eventPublisher.publish(
                kafkaTopics.userEvents(),
                userId.toString(),
                "USER_DELETED",
                principal.getAuthUserId(),
                Map.of("idUser", userId.toString())
        );
    }

    @Transactional
    public UserEligibilityResponse eligibility(AuthPrincipal principal, UUID userId) {
        UserProfile current = ensureLocalUser(principal);
        UserProfile target = findById(userId);

        if (!isAdmin(principal)
                && !target.getIdUser().equals(current.getIdUser())
                && !userTeamRepository.existsSharedTeam(principal.getAuthUserId(), target.getIdUser())) {
            throw new ForbiddenException("Eligibility is visible only for shared teams");
        }

        List<UUID> teamIds = userTeamRepository.findAllByUserIdUser(target.getIdUser())
                .stream()
                .map(ut -> ut.getTeam().getIdTeam())
                .toList();

        return new UserEligibilityResponse(
                target.getIdUser(),
                target.getNameSpecialty() == null ? null : target.getNameSpecialty().getIdNameSpecialty(),
                target.getNameSpecialty() == null ? null : target.getNameSpecialty().getNameSpecialty(),
                teamIds,
                true
        );
    }

    @Transactional
    public UserProfile ensureLocalUser(AuthPrincipal principal) {
        return userProfileRepository.findById(principal.getAuthUserId())
                .map(existing -> {
                    if (existing.getLoginUser() == null || !existing.getLoginUser().equals(principal.getUsername())) {
                        existing.setLoginUser(principal.getUsername());
                        return userProfileRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    UserProfile created = new UserProfile();
                    created.setIdUser(principal.getAuthUserId());
                    created.setNameUser(principal.getUsername());
                    created.setLoginUser(principal.getUsername());
                    created.setMidleNameUser(null);
                    try {
                        UserProfile saved = userProfileRepository.save(created);
                        publishUserEvent("USER_CREATED", principal.getAuthUserId(), saved);
                        return saved;
                    } catch (DataIntegrityViolationException ex) {
                        // Another concurrent request created the same local profile first.
                        return userProfileRepository.findById(principal.getAuthUserId())
                                .orElseThrow(() -> ex);
                    }
                });
    }

    public UserProfile findById(UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public boolean isAdmin(AuthPrincipal principal) {
        return principal.getGlobalRole() == GlobalRole.ADMIN;
    }

    public void ensureAdmin(AuthPrincipal principal) {
        if (!isAdmin(principal)) {
            throw new ForbiddenException("Admin role required");
        }
    }

    private UserResponse toUserResponse(UserProfile user) {
        List<TeamResponse> teams = userTeamRepository.findAllByUserIdUser(user.getIdUser())
                .stream()
                .map(UserTeam::getTeam)
                .map(team -> new TeamResponse(team.getIdTeam(), team.getNameTeam()))
                .toList();

        return new UserResponse(
                user.getIdUser(),
                user.getLoginUser(),
                user.getNameUser(),
                user.getMidleNameUser(),
                user.getEmailUser(),
                user.getPhoneUser(),
                user.getTelegramUser(),
                user.getNameSpecialty() == null ? null : user.getNameSpecialty().getIdNameSpecialty(),
                user.getNameSpecialty() == null ? null : user.getNameSpecialty().getNameSpecialty(),
                teams
        );
    }

    private Specification<UserProfile> searchByText(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return (root, query, cb) -> {
            String pattern = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nameUser")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("midleNameUser"), "")), pattern)
            );
        };
    }

    private Specification<UserProfile> byTeam(UUID teamId) {
        if (teamId == null) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Join<UserProfile, UserTeam> userTeams = root.join("userTeams", JoinType.INNER);
            return cb.equal(userTeams.get("team").get("idTeam"), teamId);
        };
    }

    private Specification<UserProfile> bySpecialty(UUID specialtyId) {
        if (specialtyId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("nameSpecialty").get("idNameSpecialty"), specialtyId);
    }

    private void publishUserEvent(String eventType, UUID actorId, UserProfile user) {
        eventPublisher.publish(
                kafkaTopics.userEvents(),
                user.getIdUser().toString(),
                eventType,
                actorId,
                Map.of(
                        "idUser", user.getIdUser().toString(),
                        "loginUser", user.getLoginUser() == null ? "" : user.getLoginUser(),
                        "nameUser", user.getNameUser(),
                        "midleNameUser", user.getMidleNameUser() == null ? "" : user.getMidleNameUser(),
                        "emailUser", user.getEmailUser() == null ? "" : user.getEmailUser(),
                        "phoneUser", user.getPhoneUser() == null ? "" : user.getPhoneUser(),
                        "telegramUser", user.getTelegramUser() == null ? "" : user.getTelegramUser(),
                        "nameSpecUserId", user.getNameSpecialty() == null ? "" : user.getNameSpecialty().getIdNameSpecialty().toString()
                )
        );
    }
}
