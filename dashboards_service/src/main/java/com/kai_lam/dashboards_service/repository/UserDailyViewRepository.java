package com.kai_lam.dashboards_service.repository;

import com.kai_lam.dashboards_service.model.UserDailyView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDailyViewRepository extends JpaRepository<UserDailyView, UUID> {
    Optional<UserDailyView> findByUserIdAndDate(UUID userId, LocalDate date);

    List<UserDailyView> findAllByUserIdOrderByDateAsc(UUID userId);

    List<UserDailyView> findAllByUserIdAndDateBetweenOrderByDateAsc(UUID userId, LocalDate from, LocalDate to);
}
