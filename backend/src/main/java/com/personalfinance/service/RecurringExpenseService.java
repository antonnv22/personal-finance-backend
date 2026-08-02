package com.personalfinance.service;

import com.personalfinance.domain.Account;
import com.personalfinance.domain.Category;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.domain.Currency;
import com.personalfinance.domain.OccurrenceStatus;
import com.personalfinance.domain.RecurringExpense;
import com.personalfinance.domain.RecurringExpenseOccurrence;
import com.personalfinance.domain.User;
import com.personalfinance.dto.request.CreateRecurringExpenseRequest;
import com.personalfinance.dto.request.UpdateRecurringExpenseRequest;
import com.personalfinance.dto.response.RecurringExpenseResponse;
import com.personalfinance.dto.response.UpcomingPaymentResponse;
import com.personalfinance.exception.CategoryTypeMismatchException;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.mapper.EntityMapper;
import com.personalfinance.repository.AccountRepository;
import com.personalfinance.repository.RecurringExpenseOccurrenceRepository;
import com.personalfinance.repository.RecurringExpenseRepository;
import com.personalfinance.repository.UserRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final RecurringExpenseOccurrenceRepository occurrenceRepository;
    private final AccountRepository accountRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final RecurringExpenseScheduler scheduler;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public List<RecurringExpenseResponse> getAll(UUID userId) {
        return recurringExpenseRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(entityMapper::toRecurringExpenseResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecurringExpenseResponse getById(UUID userId, UUID id) {
        return entityMapper.toRecurringExpenseResponse(findRule(userId, id));
    }

    @Transactional
    public RecurringExpenseResponse create(UUID userId, CreateRecurringExpenseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = findAccount(userId, request.accountId());
        Category category = categoryService.findCategory(userId, request.categoryId());

        validateCategoryIsExpense(category);
        validateCurrencyMatchesAccount(request.currency(), account);

        RecurringExpense rule = new RecurringExpense(
                user,
                account,
                category,
                request.name(),
                request.description(),
                request.plannedAmount(),
                request.currency(),
                request.recurrenceType(),
                request.dayOfMonth(),
                request.startDate(),
                request.endDate());
        // save() возвращает управляемый экземпляр (идентификатор задаётся вручную,
        // поэтому Spring Data делает merge) — дальше работаем именно с ним.
        RecurringExpense saved = recurringExpenseRepository.save(rule);

        // Платежи создаём сразу, чтобы правило было видно в календаре
        // не дожидаясь ночного запуска фоновой генерации.
        scheduler.generateThroughHorizon(saved);

        return entityMapper.toRecurringExpenseResponse(saved);
    }

    @Transactional
    public RecurringExpenseResponse update(UUID userId, UUID id, UpdateRecurringExpenseRequest request) {
        RecurringExpense rule = findRule(userId, id);
        Account account = findAccount(userId, request.accountId());
        Category category = categoryService.findCategory(userId, request.categoryId());

        validateCategoryIsExpense(category);
        validateCurrencyMatchesAccount(request.currency(), account);

        rule.setName(request.name());
        rule.setDescription(request.description());
        rule.setPlannedAmount(request.plannedAmount());
        rule.setCurrency(request.currency());
        rule.setAccount(account);
        rule.setCategory(category);
        rule.setRecurrenceType(request.recurrenceType());
        rule.setDayOfMonth(request.dayOfMonth());
        rule.setStartDate(request.startDate());
        rule.setEndDate(request.endDate());
        rule.setActive(request.active());

        // Ещё не подтверждённые будущие платежи пересобираем под новые параметры;
        // уже выполненные и пропущенные — историю — не трогаем.
        dropFuturePlanned(rule);
        if (rule.isActive()) {
            scheduler.generateThroughHorizon(rule);
        }

        return entityMapper.toRecurringExpenseResponse(rule);
    }

    /**
     * Удаляет правило, если по нему нет истории. Если есть выполненные платежи,
     * правило архивируется — та же логика, что у счетов в
     * {@link AccountService#delete}: связанные транзакции и отчёты должны остаться целыми.
     */
    @Transactional
    public void delete(UUID userId, UUID id) {
        RecurringExpense rule = findRule(userId, id);
        boolean hasHistory =
                occurrenceRepository.existsByRecurringExpenseIdAndStatus(rule.getId(), OccurrenceStatus.COMPLETED);

        if (hasHistory) {
            rule.setActive(false);
            dropFuturePlanned(rule);
        } else {
            recurringExpenseRepository.delete(rule);
        }
    }

    @Transactional(readOnly = true)
    public List<UpcomingPaymentResponse> getUpcoming(UUID userId, int limit) {
        LocalDate today = LocalDate.now();
        return occurrenceRepository
                .findUpcoming(userId, today, PageRequest.of(0, limit))
                .stream()
                .map(occurrence -> toUpcoming(occurrence, today))
                .toList();
    }

    RecurringExpense findRule(UUID userId, UUID id) {
        return recurringExpenseRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring expense not found"));
    }

    private Account findAccount(UUID userId, UUID accountId) {
        return accountRepository
                .findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private void dropFuturePlanned(RecurringExpense rule) {
        List<RecurringExpenseOccurrence> future =
                occurrenceRepository.findByRecurringExpenseIdAndPlannedDateGreaterThanEqual(
                        rule.getId(), LocalDate.now());
        occurrenceRepository.deleteAll(
                future.stream().filter(o -> o.getStatus() == OccurrenceStatus.PLANNED).toList());
    }

    private void validateCategoryIsExpense(Category category) {
        if (category.getType() != CategoryType.EXPENSE) {
            throw new CategoryTypeMismatchException("Recurring expense requires a category of type EXPENSE");
        }
    }

    /**
     * Transaction не хранит валюту — сумма всегда в валюте счёта. Поэтому правило
     * в EUR, оплачиваемое с рублёвого счёта, дало бы бессмысленное сравнение
     * «план 15 EUR против факта 1500 RUB».
     */
    private void validateCurrencyMatchesAccount(Currency currency, Account account) {
        if (currency != account.getCurrency()) {
            throw new IllegalArgumentException(
                    "Currency %s does not match account currency %s".formatted(currency, account.getCurrency()));
        }
    }

    private UpcomingPaymentResponse toUpcoming(RecurringExpenseOccurrence occurrence, LocalDate today) {
        RecurringExpense rule = occurrence.getRecurringExpense();
        return new UpcomingPaymentResponse(
                occurrence.getId(),
                rule.getName(),
                occurrence.getPlannedDate(),
                ChronoUnit.DAYS.between(today, occurrence.getPlannedDate()),
                occurrence.getPlannedAmount(),
                rule.getCurrency(),
                occurrence.isOverdue(today));
    }
}
