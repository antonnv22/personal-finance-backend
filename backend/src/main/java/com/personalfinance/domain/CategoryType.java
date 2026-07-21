package com.personalfinance.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category type")
public enum CategoryType {

    @Schema(description = "Money received")
    INCOME,

    @Schema(description = "Money spent")
    EXPENSE
}