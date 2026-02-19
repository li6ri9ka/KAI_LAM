package com.kai_lam.dashboards_service.controller;

import com.kai_lam.dashboards_service.dto.GanttDashboardResponse;
import com.kai_lam.dashboards_service.dto.KanbanDashboardResponse;
import com.kai_lam.dashboards_service.dto.UserDailyViewResponse;
import com.kai_lam.dashboards_service.dto.UserDailyViewUpsertRequest;
import com.kai_lam.dashboards_service.exception.ForbiddenException;
import com.kai_lam.dashboards_service.security.AuthPrincipal;
import com.kai_lam.dashboards_service.service.UserDailyViewService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/dashboards")
public class DashboardController {
    private final UserDailyViewService userDailyViewService;

    public DashboardController(UserDailyViewService userDailyViewService) {
        this.userDailyViewService = userDailyViewService;
    }

    @GetMapping("/daily")
    public UserDailyViewResponse getDaily(@AuthenticationPrincipal AuthPrincipal principal,
                                          @RequestParam(required = false) UUID userId,
                                          @RequestParam(required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UUID targetUserId = resolveTargetUser(principal, userId);
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        return userDailyViewService.getDaily(targetUserId, targetDate);
    }

    @PutMapping("/daily")
    public UserDailyViewResponse upsertDaily(@AuthenticationPrincipal AuthPrincipal principal,
                                             @RequestParam(required = false) UUID userId,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                             @Valid @RequestBody UserDailyViewUpsertRequest request) {
        UUID targetUserId = resolveTargetUser(principal, userId);
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        return userDailyViewService.upsertDaily(targetUserId, targetDate, request.todayTaskIds(), request.overdueTaskIds());
    }

    @GetMapping("/kanban")
    public KanbanDashboardResponse getKanban(@AuthenticationPrincipal AuthPrincipal principal,
                                             @RequestParam(required = false) UUID userId,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UUID targetUserId = resolveTargetUser(principal, userId);
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        return userDailyViewService.getKanban(targetUserId, targetDate);
    }

    @GetMapping("/gantt")
    public GanttDashboardResponse getGantt(@AuthenticationPrincipal AuthPrincipal principal,
                                           @RequestParam(required = false) UUID userId,
                                           @RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                           @RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        UUID targetUserId = resolveTargetUser(principal, userId);
        LocalDate rangeFrom = from == null ? LocalDate.now().minusDays(7) : from;
        LocalDate rangeTo = to == null ? LocalDate.now().plusDays(14) : to;
        return userDailyViewService.getGantt(targetUserId, rangeFrom, rangeTo);
    }

    private UUID resolveTargetUser(AuthPrincipal principal, UUID requestedUserId) {
        if (requestedUserId == null || requestedUserId.equals(principal.getAuthUserId())) {
            return principal.getAuthUserId();
        }

        String role = principal.getRole() == null ? "" : principal.getRole().trim().toUpperCase();
        if ("ADMIN".equals(role) || "TEAM_LEAD".equals(role)) {
            return requestedUserId;
        }

        throw new ForbiddenException("You can access only your own dashboard");
    }
}
