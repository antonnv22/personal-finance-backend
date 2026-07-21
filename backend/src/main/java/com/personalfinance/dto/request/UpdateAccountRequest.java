package com.personalfinance.dto.request;

import com.personalfinance.domain.AccountType;
import com.personalfinance.domain.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountRequest(
        @NotBlank String name,
        @NotNull AccountType type,
        Currency currency) {
}
