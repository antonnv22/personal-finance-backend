package com.personalfinance.dto.request;

import com.personalfinance.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull TransactionType type,
        @NotNull UUID accountId,
        @NotNull UUID categoryId,
        String description,
        @NotNull LocalDate transactionDate) {
}
