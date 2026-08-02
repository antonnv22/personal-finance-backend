package com.personalfinance.dto.response;

import com.personalfinance.domain.Currency;
import com.personalfinance.domain.RecurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Recurring expense rule")
public record RecurringExpenseResponse(
        UUID id,
        String name,
        String description,
        BigDecimal plannedAmount,
        Currency currency,
        UUID accountId,
        String accountName,
        UUID categoryId,
        String categoryName,
        RecurrenceType recurrenceType,
        int dayOfMonth,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
