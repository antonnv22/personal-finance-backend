package com.personalfinance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Конкретный запланированный платёж по правилу. Пока пользователь его не
 * подтвердил, реальной транзакции не существует.
 */
@Entity
@Table(name = "recurring_expense_occurrences")
public class RecurringExpenseOccurrence {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurring_expense_id", nullable = false)
    private RecurringExpense recurringExpense;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @Column(name = "planned_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal plannedAmount;

    @Column(name = "actual_amount", precision = 19, scale = 2)
    private BigDecimal actualAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OccurrenceStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected RecurringExpenseOccurrence() {
    }

    public RecurringExpenseOccurrence(
            RecurringExpense recurringExpense, LocalDate plannedDate, BigDecimal plannedAmount) {
        this.id = UUID.randomUUID();
        this.recurringExpense = recurringExpense;
        this.plannedDate = plannedDate;
        this.plannedAmount = plannedAmount;
        this.status = OccurrenceStatus.PLANNED;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Отметить платёж выполненным и связать с созданной транзакцией. */
    public void complete(Transaction transaction, LocalDate actualDate, BigDecimal actualAmount) {
        this.transaction = transaction;
        this.actualDate = actualDate;
        this.actualAmount = actualAmount;
        this.status = OccurrenceStatus.COMPLETED;
    }

    public void skip() {
        this.status = OccurrenceStatus.SKIPPED;
    }

    /**
     * Просрочен ли платёж: ожидается, но плановая дата уже прошла.
     * Вычисляется на лету, в базе не хранится.
     */
    public boolean isOverdue(LocalDate today) {
        return status == OccurrenceStatus.PLANNED && plannedDate.isBefore(today);
    }

    /** Отклонение факта от плана; null, пока платёж не выполнен. */
    public BigDecimal getDeviation() {
        return actualAmount == null ? null : actualAmount.subtract(plannedAmount);
    }

    public UUID getId() {
        return id;
    }

    public RecurringExpense getRecurringExpense() {
        return recurringExpense;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public LocalDate getPlannedDate() {
        return plannedDate;
    }

    public LocalDate getActualDate() {
        return actualDate;
    }

    public BigDecimal getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(BigDecimal plannedAmount) {
        this.plannedAmount = plannedAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public OccurrenceStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
