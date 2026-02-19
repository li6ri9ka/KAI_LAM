package com.kai_lam.user_service.controller;

import com.kai_lam.user_service.dto.MeUpdateRequest;
import com.kai_lam.user_service.dto.UserEligibilityResponse;
import com.kai_lam.user_service.dto.UserResponse;
import com.kai_lam.user_service.security.AuthPrincipal;
import com.kai_lam.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
        return userService.me(principal);
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal AuthPrincipal principal,
                                 @Valid @RequestBody MeUpdateRequest request) {
        return userService.updateMe(principal, request);
    }

    @GetMapping("/users/{userId}")
    public UserResponse getUser(@AuthenticationPrincipal AuthPrincipal principal,
                                @PathVariable UUID userId) {
        return userService.getUser(principal, userId);
    }

    @GetMapping("/users")
    public Page<UserResponse> listUsers(@AuthenticationPrincipal AuthPrincipal principal,
                                        @RequestParam(required = false) String q,
                                        @RequestParam(required = false) UUID teamId,
                                        @RequestParam(required = false) UUID nameSpecUserId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(defaultValue = "idUser") String sortBy,
                                        @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return userService.listUsers(principal, q, teamId, nameSpecUserId, pageable);
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@AuthenticationPrincipal AuthPrincipal principal,
                           @PathVariable UUID userId) {
        userService.deleteUser(principal, userId);
    }

    @GetMapping("/users/{userId}/eligibility")
    public UserEligibilityResponse eligibility(@AuthenticationPrincipal AuthPrincipal principal,
                                               @PathVariable UUID userId) {
        return userService.eligibility(principal, userId);
    }
}
