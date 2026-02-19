package com.kai_lam.projects_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Component
public class UserDirectoryClient {
    private static final ParameterizedTypeReference<List<UserTeamMembershipDto>> TEAM_MEMBERSHIPS_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public UserDirectoryClient(@Value("${app.user-service.base-url:http://user-service:8082}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<UUID> getUserTeamIds(UUID userId, String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return List.of();
        }

        try {
            List<UserTeamMembershipDto> memberships = restClient.get()
                    .uri("/users/{userId}/team-memberships", userId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(TEAM_MEMBERSHIPS_TYPE);

            if (memberships == null || memberships.isEmpty()) {
                return List.of();
            }

            return memberships.stream()
                    .map(UserTeamMembershipDto::idUserTeam)
                    .toList();
        } catch (RestClientException ex) {
            return List.of();
        }
    }

    public record UserTeamMembershipDto(
            UUID idUserTeam,
            UUID userId,
            UUID teamId,
            String nameTeam
    ) {
    }
}
