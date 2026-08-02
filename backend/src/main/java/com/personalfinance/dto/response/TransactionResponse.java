package com.personalfinance.dto.response;

import com.personalfinance.domain.Currency;
import com.personalfinance.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Transaction response")
public record TransactionResponse(

        @Schema(
                description = "Unique identifier of the transaction",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID id,

        @Schema(
                description = "Transaction amount",
                example = "150.50"
        )
        BigDecimal amount,

        @Schema(
                description = "Currency the amount is expressed in. Transactions have no currency "
                        + "of their own — it is always the currency of the account.",
                example = "EUR"
        )
        Currency currency,

        @Schema(
                description = "Transaction type",
                example = "EXPENSE"
        )
        TransactionType type,

        @Schema(
                description = "Account identifier",
                example = "550e8400-e29b-41d4-a716-446655440001"
        )
        UUID accountId,

        @Schema(
                description = "Account name",
                example = "Main Bank Card"
        )
        String accountName,

        @Schema(
                description = "Category identifier",
                example = "550e8400-e29b-41d4-a716-446655440002"
        )
        UUID categoryId,

        @Schema(
                description = "Category name",
                example = "Food"
        )
        String categoryName,

        @Schema(
                description = "Optional transaction description",
                example = "Lunch at restaurant"
        )
        String description,

        @Schema(
                description = "Date when transaction occurred",
                example = "2026-07-21"
        )
        LocalDate transactionDate,

        @Schema(
                description = "Date and time when transaction was created",
                example = "2026-07-21T15:30:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Date and time when transaction was last updated",
                example = "2026-07-21T16:00:00"
        )
        LocalDateTime updatedAt
) {
}