package com.personalfinance.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record BalanceResponse(Map<String, BigDecimal> balances
) {
}