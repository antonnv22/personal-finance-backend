package com.personalfinance.service;

import com.personalfinance.domain.*;
import com.personalfinance.dto.request.CreateTransactionRequest;
import com.personalfinance.dto.response.TransactionResponse;
import com.personalfinance.exception.CategoryTypeMismatchException;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.mapper.EntityMapper;
import com.personalfinance.repository.AccountRepository;
import com.personalfinance.repository.TransactionRepository;
import com.personalfinance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAll(
            UUID userId,
            UUID accountId,
            UUID categoryId,
            TransactionType type,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {
        return transactionRepository
                .findFiltered(userId, accountId, categoryId, type, from, to, pageable)
                .map(entityMapper::toTransactionResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(UUID userId, UUID transactionId) {
        Transaction transaction = findTransaction(userId, transactionId);
        return entityMapper.toTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse create(UUID userId, CreateTransactionRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = accountRepository
                .findByIdAndUserId(request.accountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Category category = categoryService.findCategory(userId, request.categoryId());

        validateTypes(request.type(), category.getType());

        Transaction transaction = new Transaction(
                user,
                account,
                category,
                request.amount(),
                request.type(),
                request.description(),
                request.transactionDate());
        transactionRepository.save(transaction);
        return entityMapper.toTransactionResponse(transaction);
    }

    @Transactional
    public void delete(UUID userId, UUID transactionId) {
        Transaction transaction = findTransaction(userId, transactionId);
        transactionRepository.delete(transaction);
    }

    private Transaction findTransaction(UUID userId, UUID transactionId) {
        return transactionRepository
                .findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private void validateTypes(TransactionType transactionType, CategoryType categoryType) {
        if (!transactionType.name().equals(categoryType.name())) {
            throw new CategoryTypeMismatchException("Transaction type must match category type");
        }
    }
}
