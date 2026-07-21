package com.personalfinance.service;

import com.personalfinance.domain.TransactionType;
import com.personalfinance.dto.response.CategoryExpenseResponse;
import com.personalfinance.dto.response.MonthlyReportResponse;
import com.personalfinance.dto.response.SummaryReportResponse;
import com.personalfinance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(UUID userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        BigDecimal income = sum(userId, TransactionType.INCOME, from, to);
        BigDecimal expense = sum(userId, TransactionType.EXPENSE, from, to);
        return new MonthlyReportResponse(year, month, income, expense, income.subtract(expense));
    }

    @Transactional(readOnly = true)
    public List<CategoryExpenseResponse> getExpensesByCategory(UUID userId, LocalDate from, LocalDate to) {
        return transactionRepository.sumExpensesByCategory(userId, from, to).stream()
                .map(row -> new CategoryExpenseResponse((String) row[0], (BigDecimal) row[1]))
                .toList();
    }

    @Transactional(readOnly = true)
    public SummaryReportResponse getSummary(UUID userId, LocalDate from, LocalDate to) {
        BigDecimal income = sum(userId, TransactionType.INCOME, from, to);
        BigDecimal expense = sum(userId, TransactionType.EXPENSE, from, to);
        return new SummaryReportResponse(from, to, income, expense, income.subtract(expense));
    }

    private BigDecimal sum(UUID userId, TransactionType type, LocalDate from, LocalDate to) {
        return transactionRepository.sumByTypeAndPeriod(userId, type, from, to);
    }
}
