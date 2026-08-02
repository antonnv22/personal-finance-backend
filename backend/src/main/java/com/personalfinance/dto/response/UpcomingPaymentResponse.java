package com.personalfinance.dto.response;

import com.personalfinance.domain.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Upcoming planned payment for the dashboard")
public record UpcomingPaymentResponse(
        UUID occurrenceId,
        String name,
        LocalDate plannedDate,

        @Schema(description = "Days from today until the payment; 0 means today", example = "3")
        long daysUntil,

        BigDecimal plannedAmount,
        Currency currency,

        @Schema(description = "True when the planned date has already passed", example = "false")
        boolean overdue) {
}
