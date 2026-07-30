package com.personalfinance.repository;

import com.personalfinance.domain.RecurringExpense;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, UUID> {

    List<RecurringExpense> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<RecurringExpense> findByUserIdAndActiveTrue(UUID userId);

    Optional<RecurringExpense> findByIdAndUserId(UUID id, UUID userId);

    /** Все активные правила — используется фоновой генерацией по расписанию. */
    List<RecurringExpense> findByActiveTrue();
}
