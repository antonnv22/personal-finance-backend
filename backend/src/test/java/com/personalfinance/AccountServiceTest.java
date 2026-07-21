package com.personalfinance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.domain.TransactionType;
import com.personalfinance.dto.response.AuthResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AccountServiceTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void shouldCalculateBalanceFromTransactions() throws Exception {
        AuthResponse auth = testDataFactory.registerUser("account-test@example.com");
        UUID accountId = testDataFactory.createAccount(auth.token(), "Main card");
        UUID incomeCategory = testDataFactory.createCategory(auth.token(), "Bonus", CategoryType.INCOME);
        UUID expenseCategory = testDataFactory.createCategory(auth.token(), "Food", CategoryType.EXPENSE);

        testDataFactory.createTransaction(auth.token(), accountId, incomeCategory, new BigDecimal("100000"), TransactionType.INCOME);
        testDataFactory.createTransaction(auth.token(), accountId, expenseCategory, new BigDecimal("5000"), TransactionType.EXPENSE);

        mockMvc.perform(get("/api/accounts/" + accountId).header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", comparesEqualTo(95000.0)));
    }
}
