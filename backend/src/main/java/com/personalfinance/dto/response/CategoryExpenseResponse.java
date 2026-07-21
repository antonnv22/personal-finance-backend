package com.personalfinance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Category expense statistics response")
public record CategoryExpenseResponse(

        @Schema(
                description = "Name of the expense category",
                example = "Food"
        )
        String categoryName,

        @Schema(
                description = "Total amount spent in this category",
                example = "1250.50"
        )
        BigDecimal amount
) {
}