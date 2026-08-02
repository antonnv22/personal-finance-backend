package com.personalfinance.service;

import com.personalfinance.domain.RecurringExpense;
import com.personalfinance.domain.RecurringExpenseOccurrence;
import com.personalfinance.repository.RecurringExpenseOccurrenceRepository;
import com.personalfinance.repository.RecurringExpenseRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Материализует будущие запланированные платежи из правил.
 *
 * <p>Генерация идемпотентна — её защищает уникальный ключ
 * {@code (recurring_expense_id, planned_date)}, поэтому вызывать её можно
 * сколько угодно раз. Это используется намеренно: платежи создаются и по
 * расписанию, и при изменении правила, и при открытии календаря. Иначе
 * только что созданное правило не показывало бы платежей до срабатывания
 * фоновой задачи.
 *
 * <p>Транзакции здесь не создаются никогда — только план.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringExpenseScheduler {

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final RecurringExpenseOccurrenceRepository occurrenceRepository;

    @Value("${app.recurring.horizon-months:12}")
    private int horizonMonths;

    /** Фоновое пополнение горизонта планирования для всех активных правил. */
    @Scheduled(cron = "${app.recurring.generation-cron:0 15 3 * * *}")
    @Transactional
    public void generateForAllActiveRules() {
        List<RecurringExpense> rules = recurringExpenseRepository.findByActiveTrue();
        int created = 0;
        for (RecurringExpense rule : rules) {
            created += generateThroughHorizon(rule).size();
        }
        log.info("Recurring expense generation finished: {} rules processed, {} occurrences created",
                rules.size(), created);
    }

    /** Генерация на горизонт по умолчанию, начиная с сегодняшнего дня. */
    @Transactional
    public List<RecurringExpenseOccurrence> generateThroughHorizon(RecurringExpense rule) {
        LocalDate today = LocalDate.now();
        return generate(rule, today, today.plusMonths(horizonMonths));
    }

    /**
     * Создаёт недостающие платежи правила в интервале дат.
     *
     * <p>Прошлое не заполняется: началом всегда берётся не раньше сегодняшнего дня,
     * иначе правило со старым startDate сразу порождало бы десятки просроченных
     * платежей.
     *
     * @return только вновь созданные платежи (уже существующие пропускаются)
     */
    @Transactional
    public List<RecurringExpenseOccurrence> generate(RecurringExpense rule, LocalDate from, LocalDate to) {
        List<RecurringExpenseOccurrence> created = new ArrayList<>();
        if (!rule.isActive() || to.isBefore(from)) {
            return created;
        }

        LocalDate effectiveFrom = maxOf(from, maxOf(rule.getStartDate(), LocalDate.now()));
        LocalDate effectiveTo = rule.getEndDate() == null ? to : minOf(to, rule.getEndDate());
        if (effectiveTo.isBefore(effectiveFrom)) {
            return created;
        }

        YearMonth cursor = YearMonth.from(effectiveFrom);
        YearMonth last = YearMonth.from(effectiveTo);

        while (!cursor.isAfter(last)) {
            LocalDate plannedDate = rule.plannedDateIn(cursor);
            cursor = cursor.plusMonths(1);

            if (plannedDate.isBefore(effectiveFrom) || plannedDate.isAfter(effectiveTo)) {
                continue;
            }
            if (!rule.coversDate(plannedDate)) {
                continue;
            }
            if (occurrenceRepository.existsByRecurringExpenseIdAndPlannedDate(rule.getId(), plannedDate)) {
                continue;
            }

            created.add(occurrenceRepository.save(
                    new RecurringExpenseOccurrence(rule, plannedDate, rule.getPlannedAmount())));
        }
        return created;
    }

    private static LocalDate maxOf(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDate minOf(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
