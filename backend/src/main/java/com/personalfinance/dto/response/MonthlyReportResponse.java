package com.personalfinance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Monthly financial report response")
public record MonthlyReportResponse(

        @Schema(
                description = "Report year",
                example = "2026"
        )
        int year,

        @Schema(
                description = "Report month (1-12)",
                example = "7"
        )
        int month,

        @Schema(
                description = "Total income amount for the month",
                example = "5000.00"
        )
        BigDecimal income,

        @Schema(
                description = "Total expense amount for the month",
                example = "2300.50"
        )
        BigDecimal expense,

        @Schema(
                description = "Balance calculated as income minus expenses",
                example = "2699.50"
        )
        BigDecimal balance
) {
}