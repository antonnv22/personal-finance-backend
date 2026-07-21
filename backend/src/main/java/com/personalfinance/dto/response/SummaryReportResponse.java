package com.personalfinance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SummaryReportResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance) {
}
