package com.personalfinance;

import com.personalfinance.domain.CategoryType;
import com.personalfinance.domain.TransactionType;
import com.personalfinance.dto.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ReportServiceTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void shouldReturnMonthlyReportAndCategoryBreakdown() throws Exception {
        AuthResponse auth = testDataFactory.registerUser("report-test@example.com");
        UUID accountId = testDataFactory.createAccount(auth.token(), "Card");
        UUID incomeCategory = testDataFactory.createCategory(auth.token(), "Salary", CategoryType.INCOME);
        UUID foodCategory = testDataFactory.createCategory(auth.token(), "Food", CategoryType.EXPENSE);
        UUID transportCategory = testDataFactory.createCategory(auth.token(), "Transport", CategoryType.EXPENSE);

        testDataFactory.createTransaction(auth.token(), accountId, incomeCategory, new BigDecimal("250000"), TransactionType.INCOME);
        testDataFactory.createTransaction(auth.token(), accountId, foodCategory, new BigDecimal("40000"), TransactionType.EXPENSE);
        testDataFactory.createTransaction(auth.token(), accountId, transportCategory, new BigDecimal("10000"), TransactionType.EXPENSE);

        YearMonth current = YearMonth.now();
        LocalDate from = current.atDay(1);
        LocalDate to = current.atEndOfMonth();

        mockMvc.perform(get("/api/reports/monthly")
                        .param("year", String.valueOf(current.getYear()))
                        .param("month", String.valueOf(current.getMonthValue()))
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income", comparesEqualTo(250000.0)))
                .andExpect(jsonPath("$.expense", comparesEqualTo(50000.0)))
                .andExpect(jsonPath("$.balance", comparesEqualTo(200000.0)));

        mockMvc.perform(get("/api/reports/expenses-by-category")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].amount", comparesEqualTo(40000.0)));
    }
}
