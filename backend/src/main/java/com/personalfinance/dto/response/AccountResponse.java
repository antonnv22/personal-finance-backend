package com.personalfinance.dto.response;

import com.personalfinance.domain.AccountType;
import com.personalfinance.domain.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String name,
        AccountType type,
        Currency currency,
        BigDecimal balance,
        boolean archived,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
