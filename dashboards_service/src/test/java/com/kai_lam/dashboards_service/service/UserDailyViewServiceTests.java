package com.kai_lam.dashboards_service.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserDailyViewServiceTests {

    @Autowired
    private UserDailyViewService userDailyViewService;

    @Test
    void tc022_userWithoutTeamGetsEmptyDailyView() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        var daily = userDailyViewService.getDaily(userId, date);

        assertThat(daily.userId()).isEqualTo(userId);
        assertThat(daily.date()).isEqualTo(date);
        assertThat(daily.todayTaskIds()).isEmpty();
        assertThat(daily.overdueTaskIds()).isEmpty();
    }
}
