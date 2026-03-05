package com.kai_lam.tasks_service.controller;

import com.kai_lam.tasks_service.security.AuthPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerValidationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void tc012_rejectsTaskCreationWithoutRequiredFields() throws Exception {
        AuthPrincipal principal = new AuthPrincipal(UUID.randomUUID(), "lead", "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        mockMvc.perform(post("/tasks")
                        .with(authentication(auth))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "infoProjectId": "%s",
                                  "descriptionTask": "Missing name"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.nameTask").exists());
    }
}
