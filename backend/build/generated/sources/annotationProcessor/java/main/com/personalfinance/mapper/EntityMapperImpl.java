package com.personalfinance.mapper;

import com.personalfinance.domain.Account;
import com.personalfinance.domain.AccountType;
import com.personalfinance.domain.Category;
import com.personalfinance.domain.CategoryType;
import com.personalfinance.domain.Currency;
import com.personalfinance.domain.Transaction;
import com.personalfinance.domain.TransactionType;
import com.personalfinance.dto.response.AccountResponse;
import com.personalfinance.dto.response.CategoryResponse;
import com.personalfinance.dto.response.TransactionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-21T17:08:56+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.5.jar, environment: Java 25.0.3 (Eclipse Adoptium)"
)
@Component
public class EntityMapperImpl implements EntityMapper {

    @Override
    public AccountResponse toAccountResponse(Account account) {
        if ( account == null ) {
            return null;
        }

        UUID id = null;
        String name = null;
        AccountType type = null;
        Currency currency = null;
        boolean archived = false;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = account.getId();
        name = account.getName();
        type = account.getType();
        currency = account.getCurrency();
        archived = account.isArchived();
        createdAt = account.getCreatedAt();
        updatedAt = account.getUpdatedAt();

        BigDecimal balance = null;

        AccountResponse accountResponse = new AccountResponse( id, name, type, currency, balance, archived, createdAt, updatedAt );

        return accountResponse;
    }

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        UUID id = null;
        String name = null;
        CategoryType type = null;
        LocalDateTime createdAt = null;

        id = category.getId();
        name = category.getName();
        type = category.getType();
        createdAt = category.getCreatedAt();

        CategoryResponse categoryResponse = new CategoryResponse( id, name, type, createdAt );

        return categoryResponse;
    }

    @Override
    public TransactionResponse toTransactionResponse(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        UUID accountId = null;
        String accountName = null;
        UUID categoryId = null;
        String categoryName = null;
        UUID id = null;
        BigDecimal amount = null;
        TransactionType type = null;
        String description = null;
        LocalDate transactionDate = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        accountId = transactionAccountId( transaction );
        accountName = transactionAccountName( transaction );
        categoryId = transactionCategoryId( transaction );
        categoryName = transactionCategoryName( transaction );
        id = transaction.getId();
        amount = transaction.getAmount();
        type = transaction.getType();
        description = transaction.getDescription();
        transactionDate = transaction.getTransactionDate();
        createdAt = transaction.getCreatedAt();
        updatedAt = transaction.getUpdatedAt();

        TransactionResponse transactionResponse = new TransactionResponse( id, amount, type, accountId, accountName, categoryId, categoryName, description, transactionDate, createdAt, updatedAt );

        return transactionResponse;
    }

    private UUID transactionAccountId(Transaction transaction) {
        Account account = transaction.getAccount();
        if ( account == null ) {
            return null;
        }
        return account.getId();
    }

    private String transactionAccountName(Transaction transaction) {
        Account account = transaction.getAccount();
        if ( account == null ) {
            return null;
        }
        return account.getName();
    }

    private UUID transactionCategoryId(Transaction transaction) {
        Category category = transaction.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getId();
    }

    private String transactionCategoryName(Transaction transaction) {
        Category category = transaction.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getName();
    }
}
