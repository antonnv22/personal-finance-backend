package com.personalfinance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Правило повторяющегося расхода — например, «Netflix, 15 EUR, каждое 5-е число».
 * Само по себе денег не двигает: порождает {@link RecurringExpenseOccurrence},
 * которые пользователь подтверждает вручную.
 */
@Entity
@Table(name = "recurring_expenses")
public class RecurringExpense {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "planned_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal plannedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false)
    private RecurrenceType recurrenceType;

    @Column(name = "day_of_month", nullable = false)
    private int dayOfMonth;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected RecurringExpense() {
    }

    public RecurringExpense(
            User user,
            Account account,
            Category category,
            String name,
            String description,
            BigDecimal plannedAmount,
            Currency currency,
            RecurrenceType recurrenceType,
            int dayOfMonth,
            LocalDate startDate,
            LocalDate endDate) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.account = account;
        this.category = category;
        this.name = name;
        this.description = description;
        this.plannedAmount = plannedAmount;
        this.currency = currency;
        this.recurrenceType = recurrenceType;
        this.dayOfMonth = dayOfMonth;
        this.startDate = startDate;
        this.endDate = endDate;
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

    /**
     * Дата платежа в указанном месяце. Если в месяце меньше дней, чем
     * {@code dayOfMonth} (31-е в феврале), дата схлопывается к последнему дню.
     */
    public LocalDate plannedDateIn(YearMonth month) {
        return month.atDay(Math.min(dayOfMonth, month.lengthOfMonth()));
    }

    /** Попадает ли дата в период действия правила. */
    public boolean coversDate(LocalDate date) {
        if (date.isBefore(startDate)) {
            return false;
        }
        return endDate == null || !date.isAfter(endDate);
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(BigDecimal plannedAmount) {
        this.plannedAmount = plannedAmount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
