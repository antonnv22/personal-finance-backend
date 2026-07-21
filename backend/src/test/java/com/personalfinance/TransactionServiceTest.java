package com.personalfinance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.domain.TransactionType;
import com.personalfinance.dto.request.CreateTransactionRequest;
import com.personalfinance.dto.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class TransactionServiceTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void shouldRejectMismatchedCategoryType() throws Exception {
        AuthResponse auth = testDataFactory.registerUser("tx-test@example.com");
        UUID accountId = testDataFactory.createAccount(auth.token(), "Cash");
        UUID expenseCategory = testDataFactory.createCategory(auth.token(), "Transport", CategoryType.EXPENSE);

        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("100"), TransactionType.INCOME, accountId, expenseCategory, null, LocalDate.now());

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateIncomeAndExpenseTransactions() throws Exception {
        AuthResponse auth = testDataFactory.registerUser("tx-create@example.com");
        UUID accountId = testDataFactory.createAccount(auth.token(), "Wallet");
        UUID incomeCategory = testDataFactory.createCategory(auth.token(), "Salary", CategoryType.INCOME);
        UUID expenseCategory = testDataFactory.createCategory(auth.token(), "Rent", CategoryType.EXPENSE);

        testDataFactory.createTransaction(auth.token(), accountId, incomeCategory, new BigDecimal("50000"), TransactionType.INCOME);
        testDataFactory.createTransaction(auth.token(), accountId, expenseCategory, new BigDecimal("15000"), TransactionType.EXPENSE);
    }
}
