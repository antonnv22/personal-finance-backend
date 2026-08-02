package com.personalfinance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Turns a planned payment into a real expense")
public record CompleteOccurrenceRequest(
        @Schema(description = "Date the payment actually happened", example = "2026-08-07")
        @NotNull LocalDate actualDate,

        @Schema(description = "Amount actually paid", example = "17.99", minimum = "0.01")
        @NotNull @DecimalMin(value = "0.01") BigDecimal actualAmount,

        @Schema(description = "Account the money left; must match the rule currency")
        @NotNull UUID accountId,

        @Schema(description = "Optional comment stored as the transaction description", example = "Netflix subscription")
        @Size(max = 500) String comment) {
}
