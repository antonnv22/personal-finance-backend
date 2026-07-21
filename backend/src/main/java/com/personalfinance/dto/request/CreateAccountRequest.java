package com.personalfinance.dto.request;

import com.personalfinance.domain.AccountType;
import com.personalfinance.domain.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating a new account")
public record CreateAccountRequest(

        @Schema(
                description = "Account name",
                example = "Main Bank Account",
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
        @NotNull
        Currency currency
) {
}
