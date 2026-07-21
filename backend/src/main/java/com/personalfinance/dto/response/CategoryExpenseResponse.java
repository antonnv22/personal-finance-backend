package com.personalfinance.dto.response;

import java.math.BigDecimal;

public record CategoryExpenseResponse(String categoryName, BigDecimal amount) {
}
