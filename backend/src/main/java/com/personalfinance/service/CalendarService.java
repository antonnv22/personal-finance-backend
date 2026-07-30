package com.personalfinance.service;

import com.personalfinance.domain.Account;
import com.personalfinance.domain.Currency;
import com.personalfinance.domain.OccurrenceStatus;
import com.personalfinance.domain.RecurringExpense;
import com.personalfinance.domain.RecurringExpenseOccurrence;
import com.personalfinance.domain.Transaction;
import com.personalfinance.domain.TransactionType;
import com.personalfinance.dto.request.CompleteOccurrenceRequest;
import com.personalfinance.dto.request.CreateTransactionRequest;
import com.personalfinance.dto.response.CalendarItemResponse;
import com.personalfinance.dto.response.CalendarMonthResponse;
import com.personalfinance.dto.response.PlannedVsActualResponse;
import com.personalfinance.exception.ConflictException;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.repository.AccountRepository;
import com.personalfinance.repository.RecurringExpenseOccurrenceRepository;
import com.personalfinance.repository.RecurringExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final RecurringExpenseOccurrenceRepository occurrenceRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final RecurringExpenseScheduler scheduler;

    @Transactional
    public CalendarMonthResponse getMonth(UUID userId, int year, int month) {
        YearMonth yearMonth = toYearMonth(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        // Ленивое досоздание: если пользователь листает вперёд дальше уже
        // сгенерированного горизонта, платежи появятся сразу. Операция
        // идемпотентна, дублей не создаст.
        recurringExpenseRepository.findByUserIdAndActiveTrue(userId)
                .forEach(rule -> scheduler.generate(rule, from, to));

        LocalDate today = LocalDate.now();
        List<CalendarItemResponse> items = occurrenceRepository.findForPeriod(userId, from, to).stream()
                .map(occurrence -> toItem(occurrence, today))
                .toList();

        return new CalendarMonthResponse(year, month, items, buildSummary(userId, from, to));
    }

    @Transactional(readOnly = true)
    public PlannedVsActualResponse getPlannedVsActual(UUID userId, int year, int month) {
        YearMonth yearMonth = toYearMonth(year, month);
        return buildSummary(userId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    /**
     * Превращает запланированный платёж в реальный расход: создаёт транзакцию
     * через {@link TransactionService} и связывает её с платежом.
     */
    @Transactional
    public CalendarItemResponse complete(UUID userId, UUID occurrenceId, CompleteOccurrenceRequest request) {
        RecurringExpenseOccurrence occurrence = findOccurrence(userId, occurrenceId);

        if (occurrence.getStatus() != OccurrenceStatus.PLANNED) {
            throw new ConflictException(
                    "Payment is already %s".formatted(occurrence.getStatus().name().toLowerCase()));
        }

        RecurringExpense rule = occurrence.getRecurringExpense();
        Account account = accountRepository
                .findByIdAndUserId(request.accountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (rule.getCurrency() != account.getCurrency()) {
            throw new IllegalArgumentException(
                    "Currency %s does not match account currency %s"
                            .formatted(rule.getCurrency(), account.getCurrency()));
        }

        String description = request.comment() == null || request.comment().isBlank()
                ? rule.getName()
                : request.comment();

        Transaction transaction = transactionService.createEntity(
                userId,
                new CreateTransactionRequest(
                        request.actualAmount(),
                        TransactionType.EXPENSE,
                        account.getId(),
                        rule.getCategory().getId(),
                        description,
                        request.actualDate()));

        occurrence.complete(transaction, request.actualDate(), request.actualAmount());
        return toItem(occurrence, LocalDate.now());
    }

    @Transactional
    public CalendarItemResponse skip(UUID userId, UUID occurrenceId) {
        RecurringExpenseOccurrence occurrence = findOccurrence(userId, occurrenceId);

        if (occurrence.getStatus() != OccurrenceStatus.PLANNED) {
            throw new ConflictException(
                    "Payment is already %s".formatted(occurrence.getStatus().name().toLowerCase()));
        }

        occurrence.skip();
        return toItem(occurrence, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public CalendarItemResponse getOccurrence(UUID userId, UUID occurrenceId) {
        return toItem(findOccurrence(userId, occurrenceId), LocalDate.now());
    }

    private RecurringExpenseOccurrence findOccurrence(UUID userId, UUID occurrenceId) {
        return occurrenceRepository
                .findByIdAndRecurringExpenseUserId(occurrenceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Planned payment not found"));
    }

    private PlannedVsActualResponse buildSummary(UUID userId, LocalDate from, LocalDate to) {
        BigDecimal planned = occurrenceRepository.sumPlannedForPeriod(userId, from, to);
        BigDecimal actual = occurrenceRepository.sumActualForPeriod(userId, from, to);

        // Суммы не конвертируются между валютами, поэтому валюту отдаём только
        // если за период она единственная. Иначе null — интерфейс покажет
        // голое число без вводящего в заблуждение символа.
        Set<Currency> currencies = occurrenceRepository.findCurrenciesForPeriod(userId, from, to);
        Currency currency = currencies.size() == 1 ? currencies.iterator().next() : null;

        return new PlannedVsActualResponse(planned, actual, actual.subtract(planned), currency);
    }

    private CalendarItemResponse toItem(RecurringExpenseOccurrence occurrence, LocalDate today) {
        RecurringExpense rule = occurrence.getRecurringExpense();
        Transaction transaction = occurrence.getTransaction();
        return new CalendarItemResponse(
                occurrence.getId(),
                rule.getId(),
                occurrence.getPlannedDate(),
                rule.getName(),
                occurrence.getPlannedAmount(),
                occurrence.getActualAmount(),
                occurrence.getDeviation(),
                rule.getCurrency(),
                occurrence.getActualDate(),
                occurrence.getStatus(),
                occurrence.isOverdue(today),
                transaction == null ? null : transaction.getId(),
                rule.getAccount().getName(),
                rule.getCategory().getName());
    }

    private YearMonth toYearMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return YearMonth.of(year, month);
    }
}
