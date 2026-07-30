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

@Schema(description = "Request for updating a recurring expense rule")
public record UpdateRecurringExpenseRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 500) String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal plannedAmount,
        @NotNull Currency currency,
        @NotNull UUID accountId,
        @NotNull UUID categoryId,
        @NotNull RecurrenceType recurrenceType,
        @NotNull @Min(1) @Max(31) Integer dayOfMonth,
        @NotNull LocalDate startDate,
        LocalDate endDate,

        @Schema(description = "Whether the rule keeps generating future payments", example = "true")
        @NotNull Boolean active) {
}
