package com.personalfinance.mapper;

import com.personalfinance.domain.Account;
import com.personalfinance.domain.Category;
import com.personalfinance.domain.RecurringExpense;
import com.personalfinance.domain.Transaction;
import com.personalfinance.dto.response.AccountResponse;
import com.personalfinance.dto.response.CategoryResponse;
import com.personalfinance.dto.response.RecurringExpenseResponse;
import com.personalfinance.dto.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    @Mapping(target = "balance", ignore = true)
    AccountResponse toAccountResponse(Account account);

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.name", target = "accountName")
    // У транзакции нет собственной валюты — она всегда равна валюте счёта.
    @Mapping(source = "account.currency", target = "currency")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    TransactionResponse toTransactionResponse(Transaction transaction);

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.name", target = "accountName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    RecurringExpenseResponse toRecurringExpenseResponse(RecurringExpense recurringExpense);
}
