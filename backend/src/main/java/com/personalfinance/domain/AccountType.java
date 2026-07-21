package com.personalfinance.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of financial account")
public enum AccountType {

    @Schema(description = "Cash account")
    CASH,

    @Schema(description = "Debit bank card account")
    DEBIT_CARD
}