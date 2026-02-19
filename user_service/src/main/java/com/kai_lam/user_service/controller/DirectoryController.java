package com.kai_lam.user_service.controller;

import com.kai_lam.user_service.dto.*;
import com.kai_lam.user_service.security.AuthPrincipal;
import com.kai_lam.user_service.service.DirectoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class DirectoryController {
    private final DirectoryService directoryService;

    public DirectoryController(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    @GetMapping("/specialties")
    public List<NameSpecialtyResponse> listSpecialties(@AuthenticationPrincipal AuthPrincipal principal) {
        return directoryService.listSpecialties(principal);
    }

    @PostMapping("/specialties")
    @ResponseStatus(HttpStatus.CREATED)
    public NameSpecialtyResponse createSpecialty(@AuthenticationPrincipal AuthPrincipal principal,
                                                 @Valid @RequestBody CreateNameSpecialtyRequest request) {
        return directoryService.createSpecialty(principal, request);
    }

    @GetMapping("/teams")
    public List<TeamResponse> listTeams(@AuthenticationPrincipal AuthPrincipal principal) {
        return directoryService.listTeams(principal);
    }

    @PostMapping("/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(@AuthenticationPrincipal AuthPrincipal principal,
                                   @Valid @RequestBody CreateTeamRequest request) {
        return directoryService.createTeam(principal, request);
    }

    @GetMapping("/users/{userId}/teams")
    public List<TeamResponse> listUserTeams(@AuthenticationPrincipal AuthPrincipal principal,
                                            @PathVariable UUID userId) {
        return directoryService.listUserTeams(principal, userId);
    }

    @GetMapping("/users/{userId}/team-memberships")
    public List<UserTeamMembershipResponse> listUserTeamMemberships(@AuthenticationPrincipal AuthPrincipal principal,
                                                                    @PathVariable UUID userId) {
        return directoryService.listUserTeamMemberships(principal, userId);
    }

    @PostMapping("/users/{userId}/teams/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addUserToTeam(@AuthenticationPrincipal AuthPrincipal principal,
                              @PathVariable UUID userId,
                              @PathVariable UUID teamId) {
        directoryService.addUserToTeam(principal, userId, teamId);
    }

    @DeleteMapping("/users/{userId}/teams/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserFromTeam(@AuthenticationPrincipal AuthPrincipal principal,
                                   @PathVariable UUID userId,
                                   @PathVariable UUID teamId) {
        directoryService.removeUserFromTeam(principal, userId, teamId);
    }
}
