package com.personalfinance.dto.response;

import com.personalfinance.domain.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Planned versus actual totals for a period")
public record PlannedVsActualResponse(
        @Schema(description = "Sum of planned amounts, excluding skipped payments", example = "500.00")
        BigDecimal plannedTotal,

        @Schema(description = "Sum of actual amounts of completed payments", example = "560.00")
        BigDecimal actualTotal,

        @Schema(description = "actualTotal - plannedTotal", example = "60.00")
        BigDecimal deviation,

        @Schema(
                description = "Currency of the totals, or null when the period mixes several "
                        + "currencies. Amounts are never converted between currencies, so a mixed "
                        + "total is only a raw sum and the UI must not label it with a currency.",
                example = "EUR")
        Currency currency) {
}
