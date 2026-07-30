package com.personalfinance.dto.response;

import com.personalfinance.domain.Currency;
import com.personalfinance.domain.OccurrenceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "A single planned or completed payment shown in the calendar")
public record CalendarItemResponse(
        @Schema(description = "Occurrence identifier")
        UUID occurrenceId,

        @Schema(description = "Rule this payment belongs to")
        UUID recurringExpenseId,

        @Schema(description = "Planned date", example = "2026-08-05")
        LocalDate date,

        @Schema(description = "Rule name", example = "Netflix")
        String name,

        @Schema(description = "Planned amount", example = "15.00")
        BigDecimal plannedAmount,

        @Schema(description = "Actual amount; null until the payment is completed", example = "17.99")
        BigDecimal actualAmount,

        @Schema(description = "Difference actual - planned; null until completed", example = "2.99")
        BigDecimal deviation,

        Currency currency,

        @Schema(description = "Date the payment actually happened", example = "2026-08-07")
        LocalDate actualDate,

        @Schema(description = "Stored status", example = "PLANNED")
        OccurrenceStatus status,

        @Schema(
                description = "True when the payment is still PLANNED but its date has passed. "
                        + "Derived, not stored — the UI renders these as OVERDUE.",
                example = "false")
        boolean overdue,

        @Schema(description = "Transaction created on completion; null otherwise")
        UUID transactionId,

        String accountName,
        String categoryName) {
}
