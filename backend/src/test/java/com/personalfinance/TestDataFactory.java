package com.personalfinance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.domain.AccountType;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.domain.Currency;
import com.personalfinance.domain.TransactionType;
import com.personalfinance.dto.request.CreateAccountRequest;
import com.personalfinance.dto.request.CreateCategoryRequest;
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
        CreateAccountRequest request = new CreateAccountRequest(name, AccountType.DEBIT_CARD, Currency.RUB);
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
}
