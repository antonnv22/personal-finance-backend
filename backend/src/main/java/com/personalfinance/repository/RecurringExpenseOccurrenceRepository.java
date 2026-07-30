package com.personalfinance.repository;

import com.personalfinance.domain.Currency;
import com.personalfinance.domain.OccurrenceStatus;
import com.personalfinance.domain.RecurringExpenseOccurrence;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecurringExpenseOccurrenceRepository
        extends JpaRepository<RecurringExpenseOccurrence, UUID> {

    /**
     * Платёж доступен только через правило, принадлежащее пользователю —
     * то же ограничение по арендатору, что и findByIdAndUserId у прочих сущностей.
     */
    Optional<RecurringExpenseOccurrence> findByIdAndRecurringExpenseUserId(UUID id, UUID userId);

    boolean existsByRecurringExpenseIdAndPlannedDate(UUID recurringExpenseId, LocalDate plannedDate);

    List<RecurringExpenseOccurrence> findByRecurringExpenseIdAndPlannedDateGreaterThanEqual(
            UUID recurringExpenseId, LocalDate from);

    boolean existsByRecurringExpenseIdAndStatus(UUID recurringExpenseId, OccurrenceStatus status);

    @Query("""
            SELECT o FROM RecurringExpenseOccurrence o
            JOIN FETCH o.recurringExpense r
            WHERE r.user.id = :userId
            AND o.plannedDate BETWEEN :from AND :to
            ORDER BY o.plannedDate ASC
            """)
    List<RecurringExpenseOccurrence> findForPeriod(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT o FROM RecurringExpenseOccurrence o
            JOIN FETCH o.recurringExpense r
            WHERE r.user.id = :userId
            AND o.status = com.personalfinance.domain.OccurrenceStatus.PLANNED
            AND o.plannedDate >= :from
            ORDER BY o.plannedDate ASC
            """)
    List<RecurringExpenseOccurrence> findUpcoming(
            @Param("userId") UUID userId, @Param("from") LocalDate from, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(o.plannedAmount), 0) FROM RecurringExpenseOccurrence o
            WHERE o.recurringExpense.user.id = :userId
            AND o.plannedDate BETWEEN :from AND :to
            AND o.status <> com.personalfinance.domain.OccurrenceStatus.SKIPPED
            """)
    BigDecimal sumPlannedForPeriod(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(o.actualAmount), 0) FROM RecurringExpenseOccurrence o
            WHERE o.recurringExpense.user.id = :userId
            AND o.plannedDate BETWEEN :from AND :to
            AND o.status = com.personalfinance.domain.OccurrenceStatus.COMPLETED
            """)
    BigDecimal sumActualForPeriod(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /** Набор валют за период — суммы осмысленны только если валюта одна. */
    @Query("""
            SELECT DISTINCT o.recurringExpense.currency FROM RecurringExpenseOccurrence o
            WHERE o.recurringExpense.user.id = :userId
            AND o.plannedDate BETWEEN :from AND :to
            AND o.status <> com.personalfinance.domain.OccurrenceStatus.SKIPPED
            """)
    Set<Currency> findCurrenciesForPeriod(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
