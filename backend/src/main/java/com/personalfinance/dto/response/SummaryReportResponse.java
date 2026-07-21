package com.personalfinance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Financial summary report response for a specified period")
public record SummaryReportResponse(

        @Schema(
                description = "Start date of the report period",
                example = "2026-01-01"
        )
        LocalDate from,

        @Schema(
                description = "End date of the report period",
                example = "2026-07-21"
        )
        LocalDate to,

        @Schema(
                description = "Total income amount for the selected period",
                example = "15000.00"
        )
        BigDecimal income,

        @Schema(
                description = "Total expense amount for the selected period",
                example = "8500.50"
        )
        BigDecimal expense,

        @Schema(
                description = "Balance calculated as income minus expenses",
                example = "6499.50"
        )
        BigDecimal balance
) {
}