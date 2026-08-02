package com.personalfinance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Calendar view of one month: payments plus planned/actual totals")
public record CalendarMonthResponse(
        int year,
        int month,
        List<CalendarItemResponse> items,

        @Schema(description = "Totals for the month, also used by the dashboard")
        PlannedVsActualResponse summary) {
}
