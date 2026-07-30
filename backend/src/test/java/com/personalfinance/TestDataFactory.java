package com.personalfinance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.domain.AccountType;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.domain.Currency;
import com.personalfinance.domain.RecurrenceType;
import com.personalfinance.domain.TransactionType;
import com.personalfinance.dto.request.CreateAccountRequest;
import com.personalfinance.dto.request.CreateCategoryRequest;
import com.personalfinance.dto.request.CreateRecurringExpenseRequest;
import com.personalfinance.dto.request.CreateTransactionRequest;
import com.personalfinance.dto.request.RegisterRequest;
import com.personalfinance.dto.response.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class TestDataFactory {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    public AuthResponse registerUser(String email) throws Exception {
        RegisterRequest request = new RegisterRequest(email, "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
    }

    public UUID createAccount(String token, String name) throws Exception {
        return createAccount(token, name, Currency.RUB);
    }

    public UUID createAccount(String token, String name, Currency currency) throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(name, AccountType.DEBIT_CARD, currency);
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
    }

    public UUID createCategory(String token, String name, CategoryType type) throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(name, type);
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
    }

    public void createTransaction(
            String token, UUID accountId, UUID categoryId, BigDecimal amount, TransactionType type)
            throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                amount, type, accountId, categoryId, "Test", LocalDate.now());
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /** Правило регулярного расхода со значениями по умолчанию: MONTHLY, активно с сегодняшнего дня. */
    public UUID createRecurringExpense(
            String token,
            String name,
            BigDecimal plannedAmount,
            Currency currency,
            UUID accountId,
            UUID categoryId,
            int dayOfMonth)
            throws Exception {
        return createRecurringExpense(
                token, name, plannedAmount, currency, accountId, categoryId, dayOfMonth, LocalDate.now(), null);
    }

    public UUID createRecurringExpense(
            String token,
            String name,
            BigDecimal plannedAmount,
            Currency currency,
            UUID accountId,
            UUID categoryId,
            int dayOfMonth,
            LocalDate startDate,
            LocalDate endDate)
            throws Exception {
        CreateRecurringExpenseRequest request = new CreateRecurringExpenseRequest(
                name,
                null,
                plannedAmount,
                currency,
                accountId,
                categoryId,
                RecurrenceType.MONTHLY,
                dayOfMonth,
                startDate,
                endDate);
        MvcResult result = mockMvc.perform(post("/api/recurring-expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
    }

    /** Идентификатор запланированного платежа правила на конкретную дату. */
    public UUID findOccurrenceId(String token, int year, int month, LocalDate plannedDate) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/calendar/" + year + "/" + month)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode items = objectMapper.readTree(result.getResponse().getContentAsString()).get("items");
        for (JsonNode item : items) {
            if (plannedDate.toString().equals(item.get("date").asText())) {
                return UUID.fromString(item.get("occurrenceId").asText());
            }
        }
        throw new AssertionError("No occurrence planned for " + plannedDate);
    }
}
