package com.personalfinance.dto.response;

import com.personalfinance.domain.AccountType;
import com.personalfinance.domain.Currency;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Account response")
public record AccountResponse(

        @Schema(
                description = "Unique identifier of the account",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID id,

        @Schema(
                description = "Account name",
                example = "Main Debit Card"
        )
        String name,

        @Schema(
                description = "Account type",
                example = "DEBIT_CARD"
        )
        AccountType type,

        @Schema(
                description = "Account currency",
                example = "EUR"
        )
        Currency currency,

        @Schema(
                description = "Current account balance",
                example = "2500.50"
        )
        BigDecimal balance,

        @Schema(
                description = "Indicates whether the account is archived",
                example = "false"
        )
        boolean archived,

        @Schema(
                description = "Date and time when the account was created",
                example = "2026-07-21T10:00:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Date and time when the account was last updated",
                example = "2026-07-21T15:30:00"
        )
        LocalDateTime updatedAt
) {
}