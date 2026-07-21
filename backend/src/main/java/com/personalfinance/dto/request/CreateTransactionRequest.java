package com.personalfinance.dto.request;

import com.personalfinance.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request for creating a new financial transaction")
public record CreateTransactionRequest(

        @Schema(
                description = "Transaction amount",
                example = "150.50",
                minimum = "0.01"
        )
        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @Schema(
                description = "Transaction type",
                example = "EXPENSE"
        )
        @NotNull
        TransactionType type,

        @Schema(
                description = "Account identifier where the transaction is recorded",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @NotNull
        UUID accountId,

        @Schema(
                description = "Category identifier for the transaction",
                example = "550e8400-e29b-41d4-a716-446655440001"
        )
        @NotNull
        UUID categoryId,

        @Schema(
                description = "Optional transaction description",
                example = "Lunch at restaurant"
        )
        String description,

        @Schema(
                description = "Transaction date",
                example = "2026-07-21"
        )
        @NotNull
        LocalDate transactionDate
) {
}