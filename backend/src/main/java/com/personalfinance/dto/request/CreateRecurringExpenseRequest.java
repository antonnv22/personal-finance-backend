package com.personalfinance.dto.request;

import com.personalfinance.domain.Currency;
import com.personalfinance.domain.RecurrenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request for creating a recurring expense rule")
public record CreateRecurringExpenseRequest(
        @Schema(description = "Rule name", example = "Netflix")
        @NotBlank @Size(max = 255) String name,

        @Schema(description = "Optional description", example = "Family subscription")
        @Size(max = 500) String description,

        @Schema(description = "Planned amount per occurrence", example = "15.00", minimum = "0.01")
        @NotNull @DecimalMin(value = "0.01") BigDecimal plannedAmount,

        @Schema(description = "Currency of the rule; must match the account currency", example = "EUR")
        @NotNull Currency currency,

        @Schema(description = "Account the payment is expected from")
        @NotNull UUID accountId,

        @Schema(description = "Expense category; must be of type EXPENSE")
        @NotNull UUID categoryId,

        @Schema(description = "Recurrence type. Only MONTHLY is supported for now", example = "MONTHLY")
        @NotNull RecurrenceType recurrenceType,

        @Schema(description = "Day of month the payment is due; clamped to the last day for short months", example = "5")
        @NotNull @Min(1) @Max(31) Integer dayOfMonth,

        @Schema(description = "First date the rule applies from", example = "2026-08-01")
        @NotNull LocalDate startDate,

        @Schema(description = "Optional last date the rule applies to", example = "2027-08-01")
        LocalDate endDate) {
}
