package com.personalfinance.dto.request;

import com.personalfinance.domain.AccountType;
import com.personalfinance.domain.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for updating an existing account")
public record UpdateAccountRequest(

        @Schema(
                description = "Account name",
                example = "Main Debit Card",
                maxLength = 50
        )
        @NotBlank
        @Size(max = 50)
        String name,

        @Schema(
                description = "Account type",
                example = "DEBIT_CARD"
        )
        @NotNull
        AccountType type,

        @Schema(
                description = "Account currency",
                example = "EUR"
        )
        Currency currency
) {
}