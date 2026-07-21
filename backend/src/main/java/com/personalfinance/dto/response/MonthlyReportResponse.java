package com.personalfinance.dto.response;

import java.math.BigDecimal;

public record MonthlyReportResponse(int year, int month, BigDecimal income, BigDecimal expense, BigDecimal balance) {
}
