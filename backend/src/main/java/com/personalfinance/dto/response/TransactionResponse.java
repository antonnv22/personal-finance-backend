package com.personalfinance.dto.response;

import com.personalfinance.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        BigDecimal amount,
        TransactionType type,
        UUID accountId,
        String accountName,
        UUID categoryId,
        String categoryName,
        String description,
        LocalDate transactionDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
