package com.personalfinance.mcp;

import com.personalfinance.config.SecurityUtils;
import com.personalfinance.domain.Currency;
import com.personalfinance.dto.response.AccountResponse;
import com.personalfinance.dto.response.BalanceResponse;
import com.personalfinance.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceTools {

    private final AccountService accountService;
    private final SecurityUtils securityUtils;

    @Tool(description = "Returns current personal finance balance for the demo user")
    public BalanceResponse getBalance() {
        //UUID userId = securityUtils.getCurrentUserId();
        UUID userId = UUID.fromString("04f98617-9d2d-456b-b94d-d90510a33f1d");

        Map<String, BigDecimal> balances =
                accountService.getAll(userId).stream()
                        .filter(account -> !account.archived())
                        .collect(Collectors.groupingBy(
                                account -> account.currency().name(),
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        AccountResponse::balance,
                                        BigDecimal::add
                                )
                        ));

        return new BalanceResponse(balances);
    }
}
